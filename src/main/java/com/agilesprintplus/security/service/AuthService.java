package com.agilesprintplus.security.service;

import com.agilesprintplus.agilesprint.api.dto.AdminUserDtos;
import com.agilesprintplus.agilesprint.api.dto.UserDtos;
import com.agilesprintplus.agilesprint.domain.*;
import com.agilesprintplus.agilesprint.exception.BadRequestException;
import com.agilesprintplus.agilesprint.exception.ConflictException;
import com.agilesprintplus.agilesprint.exception.NotFoundException;
import com.agilesprintplus.agilesprint.mapper.UserMapper;
import com.agilesprintplus.agilesprint.repo.UserRepository;
import com.agilesprintplus.security.config.JwtService;
import com.agilesprintplus.security.dto.AdminUserResponse;
import com.agilesprintplus.security.dto.AuthenticationRequest;
import com.agilesprintplus.security.dto.AuthenticationResponse;
import com.agilesprintplus.security.dto.RegisterRequest;
import com.agilesprintplus.security.entity.Token;
import com.agilesprintplus.security.entity.UserPrincipal;
import com.agilesprintplus.security.enums.TokenType;
import com.agilesprintplus.security.repo.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final UserRepository users;
  private final TokenRepository tokens;
  private final PasswordEncoder encoder;
  private final JwtService jwt;
  private final AuthenticationManager authManager;
  private final UserMapper userMapper;

  // --------------------------
  // Helpers
  // --------------------------
  /** Construit les claims applicatifs à mettre dans l’access token. */
  private Map<String, Object> buildAccessClaims(User u) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("username", u.getUsername());
    claims.put("email", u.getEmail());
    claims.put("firstName", u.getFirstName());
    claims.put("lastName", u.getLastName());
    claims.put("enabled", u.isEnabled());
    claims.put("roles", u.getRoles().stream().map(Enum::name).toList());
    if (u.getId() != null) {
      claims.put("userId", u.getId().toString());
    }
    if (u.getCreatedAt() != null) {
      long createdAtMillis = u.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
      claims.put("createdAt", createdAtMillis);
    }
    return claims;
  }

  private void saveToken(User u, String jwtToken) {
    Token t = new Token();
    t.setUser(u);
    t.setToken(jwtToken);
    t.setTokenType(TokenType.BEARER);
    t.setExpired(false);
    t.setRevoked(false);
    tokens.save(t);
  }

  private void revokeAll(User u) {
    var list = tokens.findAllValidTokenByUser(u.getId());
    list.forEach(t -> { t.setExpired(true); t.setRevoked(true); });
    tokens.saveAll(list);
  }

  private Optional<User> findByLogin(String login) {
    return users.findByEmailIgnoreCase(login)
            .or(() -> users.findByUsernameIgnoreCase(login));
  }

  private static String trimOrEmpty(String v) {
    return StringUtils.trimToEmpty(v);
  }

  // --------------------------
  // Public API
  // --------------------------
  @Transactional
  public AuthenticationResponse register(RegisterRequest req) {
    final String username = trimOrEmpty(req.username());
    final String email = trimOrEmpty(req.email()).toLowerCase(Locale.ROOT);
    final String rawPassword = StringUtils.defaultString(req.password());

    if (username.length() < 3) {
      throw new BadRequestException("Username must be at least 3 characters");
    }
    if (rawPassword.length() < 8) {
      throw new BadRequestException("Password must be at least 8 characters");
    }
    if (users.existsByUsernameIgnoreCase(username)) {
      throw new ConflictException("Username already exists");
    }
    if (users.existsByEmailIgnoreCase(email)) {
      throw new ConflictException("Email already used");
    }

    User u = User.builder()
            .username(username)
            .email(email)
            .passwordHash(encoder.encode(rawPassword))
            .roles(Set.of(Role.DEVELOPER))
            .enabled(true)
            .passwordChangeRequired(false)
            .build();

    User saved = users.save(u);

    var principal = new UserPrincipal(saved);
    Map<String, Object> claims = buildAccessClaims(saved);

    String access = jwt.generateToken(claims, principal);
    String refresh = jwt.generateRefreshToken(principal);
    saveToken(saved, access);

    return new AuthenticationResponse(access, refresh);
  }

  @Transactional
  public AuthenticationResponse login(AuthenticationRequest req) {
    final String login = trimOrEmpty(req.login());
    final String password = StringUtils.defaultString(req.password());

    authManager.authenticate(new UsernamePasswordAuthenticationToken(login, password));

    User user = findByLogin(login).orElseThrow(() -> new NotFoundException("User not found"));

    if (user.isPasswordChangeRequired()) {
      throw new BadRequestException("PASSWORD_CHANGE_REQUIRED");
    }

    revokeAll(user);

    var principal = new UserPrincipal(user);
    Map<String, Object> claims = buildAccessClaims(user);

    String access = jwt.generateToken(claims, principal);
    String refresh = jwt.generateRefreshToken(principal);
    saveToken(user, access);

    return new AuthenticationResponse(access, refresh);
  }

  @Transactional
  public AdminUserResponse  createUserWithDefaultPassword(UserDtos.CreateWithDefaultPassword req) {
    final String username = trimOrEmpty(req.username());
    final String email = trimOrEmpty(req.email()).toLowerCase(Locale.ROOT);

    if (users.existsByUsernameIgnoreCase(username)) {
      throw new ConflictException("Username already exists");
    }
    if (users.existsByEmailIgnoreCase(email)) {
      throw new ConflictException("Email already used");
    }
    User user = userMapper.toEntityWithDefaultPassword(req, encoder);
    user.setUsername(username);
    return mapToAdminResponse(users.save(user));
  }

  @Transactional
  public void changePassword(UUID userId, UserDtos.ChangePassword request, UserPrincipal currentUser) {
    boolean isSelf = currentUser.getDomainUser().getId().equals(userId);
    if (!isSelf && currentUser.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
      throw new AccessDeniedException("You can only change your own password");
    }

    User user = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    if (isSelf) {
      if (!encoder.matches(request.oldPassword(), user.getPasswordHash())) {
        throw new BadRequestException("Current password is incorrect");
      }
    }
    if (StringUtils.length(request.newPassword()) < 8) {
      throw new BadRequestException("New password must be at least 8 characters");
    }

    user.setPasswordHash(encoder.encode(request.newPassword()));
    user.setPasswordChangeRequired(!isSelf);
    users.save(user);

    revokeAll(user);
  }

  @Transactional
  public void resetUserPassword(UUID userId, UserPrincipal currentUser) {
    if (currentUser.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
      throw new AccessDeniedException("Only administrators can reset passwords");
    }
    User user = users.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    user.setPasswordHash(encoder.encode("kamer237"));
    user.setPasswordChangeRequired(true);
    users.save(user);
    revokeAll(user);
  }

  // ------- Méthodes exposées dans ton flux de "forced change" -------
  public Optional<User> findUserByEmail(String email) {
    log.info("Finding user by email: {}", email);
    return users.findByEmailIgnoreCase(email);
  }

  public boolean validatePassword(String rawPassword, String encodedPassword) {
    log.info("Validating password - raw length: {}, encoded present: {}", rawPassword.length(), encodedPassword != null);
    return encoder.matches(rawPassword, encodedPassword);
  }

  public void updatePassword(User user, String newPassword, boolean isSelf) {
    log.info("Updating password for user: {}, isSelf: {}", user.getUsername(), isSelf);
    user.setPasswordHash(encoder.encode(newPassword));
    user.setPasswordChangeRequired(false);
    users.save(user);
    log.info("Password updated successfully for user: {}", user.getUsername());
  }

  /** Après changement de mot de passe, réémet des tokens avec les claims */
  public AuthenticationResponse authenticateAfterPasswordChange(User user) {
    log.info("Authenticating after password change for user: {}", user.getUsername());
    revokeAll(user);

    var principal = new UserPrincipal(user);
    Map<String, Object> claims = buildAccessClaims(user);

    String access = jwt.generateToken(claims, principal);
    String refresh = jwt.generateRefreshToken(principal);
    saveToken(user, access);

    log.info("Tokens generated successfully for user: {}", user.getUsername());
    return new AuthenticationResponse(access, refresh);
  }

  // ------- Listing utilisateurs (inchangé) -------
  @Transactional(readOnly = true)
  public List<AdminUserDtos.Response> getUsers() {
    return users.findAllWithRoles().stream()
            .map(this::toAdminResponse)
            .collect(Collectors.toList());
  }

  private AdminUserDtos.Response toAdminResponse(User user) {
    return new AdminUserDtos.Response(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRoles(),
            user.isEnabled(),
            user.isPasswordChangeRequired(),
            user.getCreatedAt(),
            user.getUpdatedAt()
    );
  }

  private AdminUserResponse mapToAdminResponse(User user) {
    return new AdminUserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRoles(),
            user.isEnabled(),
            user.isPasswordChangeRequired(),
            user.getCreatedAt(),
            user.getUpdatedAt()
    );
  }
}

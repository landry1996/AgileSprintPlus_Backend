package com.agilesprintplus.security.controller;

import com.agilesprintplus.agilesprint.api.dto.AdminUserDtos;
import com.agilesprintplus.agilesprint.api.dto.UserDtos;
import com.agilesprintplus.agilesprint.domain.User;
import com.agilesprintplus.agilesprint.exception.BadRequestException;
import com.agilesprintplus.agilesprint.exception.NotFoundException;
import com.agilesprintplus.agilesprint.mapper.UserMapper;
import com.agilesprintplus.security.dto.*;
import com.agilesprintplus.security.entity.UserPrincipal;
import com.agilesprintplus.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService auth;
  private final UserMapper userMapper;

  @PostMapping("/register")
  public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest req) {
    return ResponseEntity.ok(auth.register(req));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest req) {
    return ResponseEntity.ok(auth.login(req));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/admin/users")
  public ResponseEntity<AdminUserResponse> createUser(@Valid @RequestBody UserDtos.CreateWithDefaultPassword req) {
    return ResponseEntity.ok(auth.createUserWithDefaultPassword(req));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/admin/users")
  public ResponseEntity<List<AdminUserDtos.Response>> getUsers() {
    return ResponseEntity.ok(auth.getUsers());
  }

  // Endpoint pour changer son propre mot de passe
  /**
   * Endpoint pour changer son propre mot de passe
   * Accessible à tout utilisateur authentifié
   */
  @PostMapping("/change-password")
  public ResponseEntity<SuccessResponse> changeOwnPassword(
          @Valid @RequestBody UserDtos.ChangePassword request,
          @AuthenticationPrincipal UserPrincipal currentUser) {

    UUID currentUserId = currentUser.getDomainUser().getId();
    auth.changePassword(currentUserId, request, currentUser);

    log.info("User {} changed their password successfully", currentUser.getUsername());
    return ResponseEntity.ok(new SuccessResponse("Password changed successfully"));
  }

  /**
   * Endpoint pour changer le mot de passe d'un utilisateur spécifique
   * Accessible aux administrateurs seulement
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/admin/users/{userId}/change-password")
  public ResponseEntity<SuccessResponse> changeUserPassword(
          @PathVariable UUID userId,
          @Valid @RequestBody UserDtos.ChangePassword request,
          @AuthenticationPrincipal UserPrincipal currentUser) {

    auth.changePassword(userId, request, currentUser);

    log.info("Admin {} changed password for user {}", currentUser.getUsername(), userId);
    return ResponseEntity.ok(new SuccessResponse("User password changed successfully"));
  }

  /**
   * Endpoint pour reset le mot de passe d'un utilisateur avec mot de passe temporaire
   * Accessible aux administrateurs seulement
   */
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/admin/users/{userId}/reset-password")
  public ResponseEntity<PasswordResetResponse> resetUserPassword(
          @PathVariable UUID userId,
          @AuthenticationPrincipal UserPrincipal currentUser) {

    auth.resetUserPassword(userId, currentUser);

    log.info("Admin {} reset password for user {}", currentUser.getUsername(), userId);
    return ResponseEntity.ok(new PasswordResetResponse(
            "Password reset successfully",
            "kamer237",
            "User will be required to change password on next login"
    ));
  }
  //-----------------------
  /**
   * Endpoint pour changement de mot de passe forcé (sans authentification)
   */
  @PostMapping("/forced-password-change")
  public ResponseEntity<AuthenticationResponse> forcedPasswordChange(
          @Valid @RequestBody ForcedPasswordChangeRequest request) {

    log.info("=== FORCED PASSWORD CHANGE STARTED ===");
    log.info("Request email: {}", request.email());
    log.info("Request oldPassword length: {}", request.oldPassword() != null ? request.oldPassword().length() : 0);
    log.info("Request newPassword length: {}", request.newPassword() != null ? request.newPassword().length() : 0);

    try {
      // Trouver l'utilisateur par email
      log.info("Searching for user with email: {}", request.email());
      Optional<User> userOpt = auth.findUserByEmail(request.email());

      if (userOpt.isEmpty()) {
        log.error("User not found with email: {}", request.email());
        throw new NotFoundException("User not found");
      }

      User user = userOpt.get();
      log.info("User found: {} (ID: {})", user.getUsername(), user.getId());
      log.info("User password change required: {}", user.isPasswordChangeRequired());

      // Vérifier que le mot de passe doit être changé
      if (!user.isPasswordChangeRequired()) {
        log.error("Password change not required for user: {}", user.getUsername());
        throw new BadRequestException("Password change not required");
      }

      // Valider l'ancien mot de passe
      log.info("Validating old password...");
      boolean isPasswordValid = auth.validatePassword(request.oldPassword(), user.getPasswordHash());
      log.info("Password validation result: {}", isPasswordValid);

      if (!isPasswordValid) {
        log.error("Old password is incorrect for user: {}", user.getUsername());
        throw new BadRequestException("Current password is incorrect");
      }

      // Changer le mot de passe
      log.info("Updating password for user: {}", user.getUsername());
      auth.updatePassword(user, request.newPassword(), true);

      // Authentifier et retourner les tokens
      log.info("Generating new tokens for user: {}", user.getUsername());
      AuthenticationResponse response = auth.authenticateAfterPasswordChange(user);

      log.info("=== FORCED PASSWORD CHANGE SUCCESSFUL ===");
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("=== FORCED PASSWORD CHANGE FAILED ===");
      log.error("Error: {}", e.getMessage(), e);
      throw e;
    }
  }
  //-----------------------

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/me")
  public ResponseEntity<UserDtos.Response> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
    User user = currentUser.getDomainUser();
    UserDtos.Response response = userMapper.toResponse(user);
    return ResponseEntity.ok(response);
  }

}
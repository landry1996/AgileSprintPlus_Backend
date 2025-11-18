package com.agilesprintplus.security.config;

import com.agilesprintplus.security.repo.TokenRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final UserDetailsService userDetailsService;
  private final TokenRepository tokenRepository;
  private final JwtService jwtService;

  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  // Endpoints publics seulement
  private static final String[] PUBLIC_ENDPOINTS = {
          "/api/auth/login",
          "/api/auth/register",
          "/api/auth/refresh",
          "/api/auth/forced-password-change",
          "/v3/api-docs/**",
          "/swagger-ui/**",
          "/swagger-ui.html"
  };

  private boolean isPublic(String path) {
    for (String p : PUBLIC_ENDPOINTS) {
      if (PATH_MATCHER.match(p, path)) return true;
    }
    return false;
  }

  @Override
  protected void doFilterInternal(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull FilterChain filterChain
  ) throws ServletException, IOException {

    String path = request.getServletPath();
    log.info("🔐 Processing request: {}", path);

    // Les endpoints publics passent directement
    if (isPublic(path)) {
      log.info("✅ Public endpoint, skipping authentication");
      filterChain.doFilter(request, response);
      return;
    }

    String header = request.getHeader("Authorization");

    // Pour les endpoints protégés, un token est REQUIS
    if (header == null || !header.startsWith("Bearer ")) {
      log.warn("❌ No Bearer token found for protected endpoint: {}", path);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("{\"error\": \"Missing or invalid authorization token\"}");
      return;
    }

    String jwt = header.substring(7);
    log.info("🔑 JWT token found, subject: {}", jwtService.extractUsername(jwt));

    try {
      String subject = jwtService.extractUsername(jwt);
      if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails user = userDetailsService.loadUserByUsername(subject);
        log.info("👤 Loaded user: {} with authorities: {}", user.getUsername(), user.getAuthorities());

        boolean isValidToken = tokenRepository.findByToken(jwt)
                .map(t -> !t.isExpired() && !t.isRevoked())
                .orElse(false);

        if (isValidToken && jwtService.isTokenValid(jwt, user)) {
          var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
          auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(auth);
          log.info("✅ User authenticated successfully: {} with roles: {}",
                  user.getUsername(), user.getAuthorities());
        } else {
          log.warn("❌ Token validation failed for user: {}", subject);
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
          return;
        }
      }
    } catch (Exception e) {
      log.error("🚨 Error during authentication", e);
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("{\"error\": \"Authentication failed: " + e.getMessage() + "\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
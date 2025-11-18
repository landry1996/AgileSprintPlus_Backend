package com.agilesprintplus.security.config;

import com.agilesprintplus.security.repo.TokenRepository;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
  private final TokenRepository tokenRepository;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) return;

    String jwt = header.substring(7);
    tokenRepository.findByToken(jwt).ifPresent(tok -> {
      tok.setExpired(true);
      tok.setRevoked(true);
      tokenRepository.save(tok);
    });
    SecurityContextHolder.clearContext();
  }
}

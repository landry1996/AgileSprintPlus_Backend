package com.agilesprintplus.security.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

  private final JwtProps props;

  public String extractUsername(@NonNull String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(@NonNull String token, @NonNull Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /** Génère un token SANS claims (compat) */
  public String generateToken(@NonNull org.springframework.security.core.userdetails.UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  /** Génère un token AVEC claims (rôles, username, etc.) */
  public String generateToken(@NonNull Map<String, Object> extraClaims,
                              @NonNull org.springframework.security.core.userdetails.UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, props.getExpiration());
  }

  /** Refresh token: pas besoin des claims applicatifs */
  public String generateRefreshToken(@NonNull org.springframework.security.core.userdetails.UserDetails userDetails) {
    long refreshExp = props.getRefreshToken() != null ? props.getRefreshToken().getExpiration() : 0L;
    return buildToken(new HashMap<>(), userDetails, refreshExp);
  }

  public boolean isTokenValid(@NonNull String token,
                              @NonNull org.springframework.security.core.userdetails.UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private String buildToken(@NonNull Map<String, Object> extraClaims,
                            @NonNull org.springframework.security.core.userdetails.UserDetails userDetails,
                            long expirationMillis) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(now + expirationMillis))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
            .compact();
  }

  private boolean isTokenExpired(@NonNull String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(@NonNull String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(@NonNull String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
  }

  /**
   * Supporte :
   * 1) Clé HEX
   * 2) Base64
   * 3) Octets bruts (UTF-8) (>= 32 bytes)
   */
  private Key getSignInKey() {
    String secretKey = props.getSecretKey();
    if (!StringUtils.hasText(secretKey)) {
      throw new IllegalStateException("JWT secret key is missing (application.security.jwt.secret-key)");
    }
    secretKey = secretKey.trim();
    if (isHex(secretKey)) {
      byte[] keyBytes = hexToBytes(secretKey);
      ensureMinSize256Bits(keyBytes, "HEX");
      log.debug("Using HEX secret key - length: {} bytes ({} bits)", keyBytes.length, keyBytes.length * 8);
      return Keys.hmacShaKeyFor(keyBytes);
    }
    if (isBase64(secretKey)) {
      try {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        ensureMinSize256Bits(keyBytes, "Base64");
        log.debug("Using Base64 secret key - length: {} bytes ({} bits)", keyBytes.length, keyBytes.length * 8);
        return Keys.hmacShaKeyFor(keyBytes);
      } catch (IllegalArgumentException e) {
        log.warn("Base64 decoding failed, falling back to raw bytes: {}", e.getMessage());
      }
    }
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    ensureMinSize256Bits(keyBytes, "UTF-8");
    log.debug("Using UTF-8 secret key - length: {} bytes ({} bits)", keyBytes.length, keyBytes.length * 8);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  private static boolean isHex(String s) {
    if (s == null || s.length() % 2 != 0) return false;
    String cleanHex = s.startsWith("0x") || s.startsWith("0X") ? s.substring(2) : s;
    return cleanHex.matches("^[0-9A-Fa-f]+$");
  }

  private static boolean isBase64(String s) {
    if (s == null || s.isEmpty()) return false;
    if (s.length() % 4 != 0) return false;
    return s.matches("^[A-Za-z0-9+/]*={0,2}$");
  }

  private static byte[] hexToBytes(String hex) {
    if (hex == null || hex.isEmpty()) {
      throw new IllegalArgumentException("Hex string cannot be null or empty");
    }
    String cleanHex = hex.startsWith("0x") || hex.startsWith("0X") ? hex.substring(2) : hex;
    if (cleanHex.length() % 2 != 0) {
      throw new IllegalArgumentException("Hex string must have even length");
    }
    int len = cleanHex.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      int hi = Character.digit(cleanHex.charAt(i), 16);
      int lo = Character.digit(cleanHex.charAt(i + 1), 16);
      if (hi < 0 || lo < 0) {
        throw new IllegalArgumentException(
                String.format("Invalid hex character at position %d-%d in secret key", i, i+1)
        );
      }
      data[i / 2] = (byte) ((hi << 4) + lo);
    }
    return data;
  }

  private static void ensureMinSize256Bits(byte[] keyBytes, String keyType) {
    if (keyBytes == null) {
      throw new IllegalStateException(keyType + " secret key bytes cannot be null");
    }
    if (keyBytes.length < 32) {
      throw new IllegalStateException(
              String.format(
                      "JWT %s secret must be at least 256 bits (32 bytes). Current: %d bytes (%d bits)",
                      keyType, keyBytes.length, keyBytes.length * 8
              )
      );
    }
    if (keyBytes.length == 32) {
      log.warn("JWT secret is exactly 256 bits. Consider using a longer key (384 or 512 bits) for better security.");
    }
  }
}

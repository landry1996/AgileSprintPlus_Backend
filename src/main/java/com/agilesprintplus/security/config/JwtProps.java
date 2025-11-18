package com.agilesprintplus.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.security.jwt")
@Getter
@Setter
public class JwtProps {
  private String secretKey;
  private long expiration;
  private Refresh refreshToken = new Refresh();
  @Getter @Setter
  public static class Refresh {
    private long expiration;
  }
}



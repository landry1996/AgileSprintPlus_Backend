package com.agilesprintplus.security.entity;

import com.agilesprintplus.agilesprint.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public final class UserPrincipal implements UserDetails {
  private final User user;

  public Collection<? extends GrantedAuthority> getAuthorities() {
    return user.getRoles().stream()
            .flatMap(role -> {
              // Inclure les permissions
              Stream<GrantedAuthority> permissions = role.getPermissions().stream()
                      .map(permission -> new SimpleGrantedAuthority(permission.getPermission()));
              // Inclure le rôle avec le préfixe ROLE_
              Stream<GrantedAuthority> roleAuthority = Stream.of(
                      new SimpleGrantedAuthority("ROLE_" + role.name())
              );
              return Stream.concat(permissions, roleAuthority);
            })
            .collect(Collectors.toList());
  }

  @Override public String getPassword() { return user.getPasswordHash(); }

  // On choisit de se connecter via email par défaut.
  @Override public String getUsername() { return user.getEmail(); }

  @Override public boolean isAccountNonExpired() { return true; }
  @Override public boolean isAccountNonLocked() { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled() { return user.isEnabled(); }

  public User getDomainUser() { return user; }
}

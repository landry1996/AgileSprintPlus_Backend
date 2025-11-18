package com.agilesprintplus.agilesprint.domain;

import com.agilesprintplus.security.enums.Permission;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public enum Role {

    ADMIN(Set.of(
            Permission.USER_READ, Permission.USER_CREATE, Permission.USER_UPDATE, Permission.USER_DELETE,
            Permission.TASK_READ, Permission.TASK_CREATE, Permission.TASK_UPDATE, Permission.TASK_DELETE,
            Permission.SPRINT_READ, Permission.SPRINT_CREATE, Permission.SPRINT_UPDATE, Permission.SPRINT_DELETE,
            Permission.GOAL_READ, Permission.GOAL_CREATE, Permission.GOAL_UPDATE, Permission.GOAL_DELETE
    )),

    PRODUCT_OWNER(Set.of(
            Permission.TASK_READ, Permission.TASK_CREATE, Permission.TASK_UPDATE,
            Permission.SPRINT_READ, Permission.SPRINT_CREATE, Permission.SPRINT_UPDATE,
            Permission.GOAL_READ, Permission.GOAL_CREATE, Permission.GOAL_UPDATE
    )),

    SCRUM_MASTER(Set.of(
            Permission.SPRINT_READ, Permission.SPRINT_CREATE, Permission.SPRINT_UPDATE,
            Permission.TASK_READ, Permission.TASK_UPDATE,
            Permission.USER_READ
    )),

    DEVELOPER(Set.of(
            Permission.TASK_READ, Permission.TASK_UPDATE,
            Permission.GOAL_READ
    )),

    TESTER(Set.of(
            Permission.TASK_READ,
            Permission.GOAL_READ
    )),

    STAKEHOLDER(Set.of(
            Permission.GOAL_READ,
            Permission.SPRINT_READ
    ));

    @Getter
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>(
                permissions.stream()
                        .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                        .toList()
        );
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}

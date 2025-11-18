package com.agilesprintplus.security.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public enum Permission {

    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_CREATE("user:create"),
    USER_DELETE("user:delete"),

    TASK_READ("task:read"),
    TASK_UPDATE("task:update"),
    TASK_CREATE("task:create"),
    TASK_DELETE("task:delete"),

    SPRINT_READ("sprint:read"),
    SPRINT_UPDATE("sprint:update"),
    SPRINT_CREATE("sprint:create"),
    SPRINT_DELETE("sprint:delete"),

    GOAL_READ("goal:read"),
    GOAL_UPDATE("goal:update"),
    GOAL_CREATE("goal:create"),
    GOAL_DELETE("goal:delete");

    @Getter
    private final String permission;
}

package com.agilesprintplus.service.taskcount;

import com.agilesprintplus.agilesprint.service.taskcount.SprintTaskCount;

import java.util.UUID;

/**
 * Implémentation concrète de SprintTaskCount utilisée
 * notamment dans les requêtes constructor expressions.
 */
public record SprintTaskCountImpl(UUID sprintId, Long completedTaskCount)
        implements SprintTaskCount {}
package com.agilesprintplus.agilesprint.service.metriques;

import java.util.UUID;

public interface SprintVelocityView {
    UUID getSprintId();
    Long getDoneTasks();
    Long getTotalTasks();
    Long getDoneStoryPoints();
    Long getTotalStoryPoints();
}
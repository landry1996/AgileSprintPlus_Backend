package com.agilesprintplus.agilesprint.service.gamification;

import java.util.UUID;

public interface GamificationLeaderboard {
    UUID getUserId();
    int getXp();
    int getBadges();
    int getTasksDone();
    int getSprintsCompleted();
}
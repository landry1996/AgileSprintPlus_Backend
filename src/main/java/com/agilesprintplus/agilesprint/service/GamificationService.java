package com.agilesprintplus.agilesprint.service;

import com.agilesprintplus.agilesprint.api.dto.GamificationDtos;
import com.agilesprintplus.agilesprint.service.gamification.GamificationLeaderboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GamificationService {

    GamificationDtos.Response profile(UUID userId);

    GamificationDtos.Response gainXp(UUID userId, int delta);

    GamificationDtos.Response awardBadge(UUID userId, int count);

    GamificationDtos.Response computeOnTaskDone(UUID userId, int storyPoints);

    GamificationDtos.Response onSprintCompleted(UUID userId);

    Page<GamificationLeaderboard> leaderboard(String usernameFilter, Pageable pageable);

    GamificationDtos.Response reset(UUID userId);

    long sumXp();
    double avgXp();
}
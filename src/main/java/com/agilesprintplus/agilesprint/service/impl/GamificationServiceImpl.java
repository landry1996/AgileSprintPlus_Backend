package com.agilesprintplus.agilesprint.service.impl;

import com.agilesprintplus.agilesprint.api.dto.GamificationDtos;
import com.agilesprintplus.agilesprint.domain.GamificationProfile;
import com.agilesprintplus.agilesprint.domain.User;
import com.agilesprintplus.agilesprint.exception.BadRequestException;
import com.agilesprintplus.agilesprint.exception.NotFoundException;
import com.agilesprintplus.agilesprint.repo.GamificationProfileRepository;
import com.agilesprintplus.agilesprint.repo.UserRepository;
import com.agilesprintplus.agilesprint.service.GamificationService;
import com.agilesprintplus.agilesprint.service.gamification.GamificationLeaderboard;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class GamificationServiceImpl implements GamificationService {
  private final GamificationProfileRepository profileRepo;
  private final UserRepository userRepo;
  @Value("${gamification.xp.perStoryPoint:10}")
  private int xpPerStoryPoint;
  @Value("${gamification.bonus.sprintCompleted.xp:50}")
  private int bonusXpPerSprint;
  @Value("${gamification.bonus.sprintCompleted.badges:1}")
  private int bonusBadgesPerSprint;

  @Override
  @Transactional(readOnly = true)
  public GamificationDtos.Response profile(UUID userId) {
    var p = profileRepo.findByUser_Id(userId)
            .orElseGet(() -> createIfMissing(userId));
    return toResponse(p);
  }
  @Override
  public GamificationDtos.Response gainXp(UUID userId, int delta) {
    ensureUserExists(userId);
    int updated = profileRepo.addXp(userId, delta);
    if (updated == 0) {
      createIfMissing(userId);
      profileRepo.addXp(userId, delta);
    }
    return profile(userId);
  }
  @Override
  public GamificationDtos.Response awardBadge(UUID userId, int count) {
    ensureUserExists(userId);
    int updated = profileRepo.addBadges(userId, count);
    if (updated == 0) {
      createIfMissing(userId);
      profileRepo.addBadges(userId, count);
    }
    return profile(userId);
  }
  @Override
  public GamificationDtos.Response computeOnTaskDone(UUID userId, int storyPoints) {
    ensureUserExists(userId);
    if (storyPoints < 0) {
      throw new BadRequestException("storyPoints must be >= 0");
    }

    int updated = profileRepo.addTasksDone(userId, 1);
    if (updated == 0) {
      createIfMissing(userId);
      profileRepo.addTasksDone(userId, 1);
    }
    int xpGain = storyPoints * xpPerStoryPoint;
    profileRepo.addXp(userId, xpGain);

    return profile(userId);
  }
  @Override
  public GamificationDtos.Response onSprintCompleted(UUID userId) {
    ensureUserExists(userId);
    int updated = profileRepo.addSprintsCompleted(userId, 1);
    if (updated == 0) {
      createIfMissing(userId);
      profileRepo.addSprintsCompleted(userId, 1);
    }

    if (bonusXpPerSprint != 0) {
      profileRepo.addXp(userId, bonusXpPerSprint);
    }
    if (bonusBadgesPerSprint != 0) {
      profileRepo.addBadges(userId, bonusBadgesPerSprint);
    }
    return profile(userId);
  }
  @Override
  @Transactional(readOnly = true)
  public Page<GamificationLeaderboard> leaderboard(String usernameFilter, Pageable pageable) {
    return profileRepo.leaderboard(usernameFilter, pageable);
  }
  @Override
  public GamificationDtos.Response reset(UUID userId) {
    ensureUserExists(userId);
    int updated = profileRepo.resetProfile(userId);
    if (updated == 0) {

      createIfMissing(userId);
    }
    return profile(userId);
  }
  @Override
  @Transactional(readOnly = true)
  public long sumXp() {
    return profileRepo.sumXp();
  }
  @Override
  @Transactional(readOnly = true)
  public double avgXp() {
    return profileRepo.avgXp();
  }
  private void ensureUserExists(UUID userId) {
    if (userId == null) throw new BadRequestException("userId is required");
    if (!userRepo.existsById(userId)) {
      throw new NotFoundException("User not found: " + userId);
    }
  }
  private GamificationProfile createIfMissing(UUID userId) {
    return profileRepo.findByUser_Id(userId).orElseGet(() -> {
      User user = userRepo.findById(userId)
              .orElseThrow(() -> new NotFoundException("User not found: " + userId));
      GamificationProfile p = new GamificationProfile();
      p.setUser(user);
      p.setXp(0);
      p.setBadges(0);
      p.setTasksDone(0);
      p.setSprintsCompleted(0);
      return profileRepo.save(p);
    });
  }

  private GamificationDtos.Response toResponse(GamificationProfile p) {
    return new GamificationDtos.Response(
            p.getUser().getId(),
            p.getXp(),
            p.getBadges(),
            p.getTasksDone(),
            p.getSprintsCompleted()
    );
  }
}

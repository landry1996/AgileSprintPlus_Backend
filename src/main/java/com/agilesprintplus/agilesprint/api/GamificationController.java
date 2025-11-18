package com.agilesprintplus.agilesprint.api;
import com.agilesprintplus.agilesprint.api.dto.GamificationDtos;
import com.agilesprintplus.agilesprint.service.GamificationService;
import com.agilesprintplus.agilesprint.service.gamification.GamificationLeaderboard;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

@RestController
@RequestMapping("/api/gamification")
@CrossOrigin("*")
@RequiredArgsConstructor
public class GamificationController {

  private final GamificationService gamificationService;

  /** Récupère le profil gamification d’un utilisateur */
  @PreAuthorize("hasAnyRole('ADMIN','PRODUCT_OWNER','SCRUM_MASTER','DEVELOPER','TESTER','STAKEHOLDER')")
  @GetMapping("/profile/{userId}")
  public ResponseEntity<GamificationDtos.Response> getProfile(@PathVariable("userId") UUID userId) {
    return ResponseEntity.ok(gamificationService.profile(userId));
  }

  /** Leaderboard des utilisateurs les plus actifs */
  @PreAuthorize("hasAnyRole('ADMIN','PRODUCT_OWNER','SCRUM_MASTER','DEVELOPER','TESTER','STAKEHOLDER')")
  @GetMapping("/leaderboard")
  public ResponseEntity<Page<GamificationLeaderboard>> leaderboard(
          @RequestParam(name = "usernameFilter", required = false) String usernameFilter,
          Pageable pageable) {
    return ResponseEntity.ok(gamificationService.leaderboard(usernameFilter, pageable));
  }

  /** Ajoute de l’XP à un utilisateur (manuellement ou par action spécifique) */
  @PreAuthorize("hasAnyRole('ADMIN','PRODUCT_OWNER','SCRUM_MASTER')")
  @PostMapping("/{userId}/gain-xp")
  public ResponseEntity<GamificationDtos.Response> gainXp(
          @PathVariable("userId") UUID userId,
          @RequestParam @Min(1) int xp) {
    return ResponseEntity.ok(gamificationService.gainXp(userId, xp));
  }

  /** Attribue un ou plusieurs badges à un utilisateur */
  @PreAuthorize("hasAnyRole('ADMIN','PRODUCT_OWNER','SCRUM_MASTER')")
  @PostMapping("/{userId}/award-badge")
  public ResponseEntity<GamificationDtos.Response> awardBadge(
          @PathVariable("userId") UUID userId,
          @RequestParam(defaultValue = "1") @Min(1) int count) {
    return ResponseEntity.ok(gamificationService.awardBadge(userId, count));
  }

  /** Calcul automatique à la fin d’une tâche terminée */
  @PreAuthorize("hasAnyRole('ADMIN','PRODUCT_OWNER','SCRUM_MASTER')")
  @PostMapping("/{userId}/task-done")
  public ResponseEntity<GamificationDtos.Response> computeOnTaskDone(
          @PathVariable("userId") UUID userId,
          @RequestParam(defaultValue = "1") @Min(0) int storyPoints) {
    return ResponseEntity.ok(gamificationService.computeOnTaskDone(userId, storyPoints));
  }

  /** Calcul automatique à la fin d’un sprint complété */
  @PreAuthorize("hasAnyRole('ADMIN','PRODUCT_OWNER','SCRUM_MASTER')")
  @PostMapping("/{userId}/sprint-completed")
  public ResponseEntity<GamificationDtos.Response> onSprintCompleted(@PathVariable("userId") UUID userId) {
    return ResponseEntity.ok(gamificationService.onSprintCompleted(userId));
  }

  /** Total cumulé d’XP de tous les utilisateurs */
  @PreAuthorize("hasAnyRole('ADMIN','PRODUCT_OWNER','SCRUM_MASTER','DEVELOPER','TESTER','STAKEHOLDER')")
  @GetMapping("/stats/sum-xp")
  public ResponseEntity<Long> sumXp() {
    return ResponseEntity.ok(gamificationService.sumXp());
  }

  /** Moyenne d’XP sur l’ensemble des utilisateurs */
  @PreAuthorize("hasAnyRole('ADMIN','PRODUCT_OWNER','SCRUM_MASTER','DEVELOPER','TESTER','STAKEHOLDER')")
  @GetMapping("/stats/avg-xp")
  public ResponseEntity<Double> avgXp() {
    return ResponseEntity.ok(gamificationService.avgXp());
  }

  /** Réinitialise le profil gamification d’un utilisateur */
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/{userId}/reset")
  public ResponseEntity<GamificationDtos.Response> reset(@PathVariable("userId") UUID userId) {
    return ResponseEntity.ok(gamificationService.reset(userId));
  }
}

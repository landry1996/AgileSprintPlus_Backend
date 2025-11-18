package com.agilesprintplus.agilesprint.repo;
import com.agilesprintplus.agilesprint.domain.GamificationProfile;
import com.agilesprintplus.agilesprint.service.gamification.GamificationLeaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface GamificationProfileRepository extends JpaRepository<GamificationProfile, UUID>, JpaSpecificationExecutor<GamificationProfile> {

    Optional<GamificationProfile> findByUser_Id(UUID userId);
    boolean existsByUser_Id(UUID userId);
    @Query(value = """
    SELECT 
        p.user_id AS userId,
        p.xp AS xp,
        p.badges AS badges,
        p.tasks_done AS tasksDone,
        p.sprints_completed AS sprintsCompleted
    FROM gamification_profile p
    JOIN users u ON p.user_id = u.id
    WHERE (:username IS NULL OR LOWER(CAST(u.username AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:username AS TEXT), '%')))
    ORDER BY p.xp DESC, p.badges DESC, p.tasks_done DESC
    """, nativeQuery = true)
    Page<GamificationLeaderboard> leaderboard(@Param("username") String username, Pageable pageable);
    Page<GamificationProfile> findAllByOrderByXpDescBadgesDescTasksDoneDesc(Pageable pageable);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE GamificationProfile p SET p.xp = p.xp + :delta WHERE p.user.id = :userId")
    int addXp(@Param("userId") UUID userId, @Param("delta") int delta);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE GamificationProfile p SET p.badges = p.badges + :delta WHERE p.user.id = :userId")
    int addBadges(@Param("userId") UUID userId, @Param("delta") int delta);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE GamificationProfile p SET p.tasksDone = p.tasksDone + :delta WHERE p.user.id = :userId")
    int addTasksDone(@Param("userId") UUID userId, @Param("delta") int delta);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE GamificationProfile p SET p.sprintsCompleted = p.sprintsCompleted + :delta WHERE p.user.id = :userId")
    int addSprintsCompleted(@Param("userId") UUID userId, @Param("delta") int delta);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           UPDATE GamificationProfile p
           SET p.xp=0, p.badges=0, p.tasksDone=0, p.sprintsCompleted=0
           WHERE p.user.id = :userId
           """)
    int resetProfile(@Param("userId") UUID userId);
    @Query("SELECT COALESCE(SUM(p.xp), 0) FROM GamificationProfile p")
    long sumXp();
    @Query("SELECT COALESCE(AVG(p.xp), 0) FROM GamificationProfile p")
    double avgXp();
}

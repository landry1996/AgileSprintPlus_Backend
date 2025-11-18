package com.agilesprintplus.agilesprint.repo;

import com.agilesprintplus.agilesprint.domain.Sprint;
import com.agilesprintplus.agilesprint.service.metriques.SprintTaskStatusCount;
import com.agilesprintplus.agilesprint.service.metriques.SprintVelocityView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
public interface SprintRepository extends JpaRepository<Sprint, UUID> {
    Optional<Sprint> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    @Query("""
           SELECT s FROM Sprint s
           WHERE (:keyword IS NULL OR
                  LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(s.goal, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    Page<Sprint> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT s FROM Sprint s WHERE s.startDate <= :today AND s.endDate >= :today ORDER BY s.startDate DESC")
    List<Sprint> findActive(@Param("today") LocalDate today);
    Optional<Sprint> findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(LocalDate today1, LocalDate today2);
    @Query("""
           SELECT s FROM Sprint s
           WHERE s.endDate   >= :start
             AND s.startDate <= :end
           """)
    List<Sprint> findOverlapping(@Param("start") LocalDate start, @Param("end") LocalDate end);
    @Query("""
           SELECT t.status AS status, COUNT(t) AS total
           FROM Task t
           WHERE t.sprint.id = :sprintId
           GROUP BY t.status
           """)
    List<SprintTaskStatusCount> countTasksByStatus(@Param("sprintId") UUID sprintId);
    @Query("""
           SELECT 
             s.id AS sprintId,
             SUM(CASE WHEN t.status = com.agilesprintplus.agilesprint.domain.TaskStatus.DONE THEN 1 ELSE 0 END)         AS doneTasks,
             COUNT(t)                                                                                                     AS totalTasks,
             SUM(CASE WHEN t.status = com.agilesprintplus.agilesprint.domain.TaskStatus.DONE THEN COALESCE(t.storyPoints,0) ELSE 0 END) AS doneStoryPoints,
             SUM(COALESCE(t.storyPoints,0))                                                                               AS totalStoryPoints
           FROM Sprint s
           LEFT JOIN Task t ON t.sprint = s
           WHERE s.id = :sprintId
           GROUP BY s.id
           """)
    Optional<SprintVelocityView> getVelocity(@Param("sprintId") UUID sprintId);
}
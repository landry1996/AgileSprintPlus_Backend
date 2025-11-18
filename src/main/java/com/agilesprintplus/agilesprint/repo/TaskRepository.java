package com.agilesprintplus.agilesprint.repo;

import com.agilesprintplus.agilesprint.api.dto.UserDtos;
import com.agilesprintplus.agilesprint.domain.Task;
import com.agilesprintplus.agilesprint.domain.TaskStatus;
import com.agilesprintplus.agilesprint.domain.User;
import com.agilesprintplus.agilesprint.service.taskcount.SprintTaskCount;
import org.mapstruct.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByTitleIgnoreCase(String titleTask);
    List<Task> findByTitleContainingIgnoreCase(String keyword);
    boolean existsByTitleIgnoreCase(String titleTask);
    Page<Task> findBySprint_Id(UUID sprintId, Pageable pageable);
    long countBySprint_Id(UUID sprintId);

    @Query("""
           SELECT t FROM Task t
           WHERE (:keyword IS NULL OR
                  LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                  LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
             AND (:sprintId IS NULL OR t.sprint.id = :sprintId)
             AND (:status IS NULL OR t.status = :status)
           """)
    Page<Task> search(@Param("keyword") String keyword,
                      @Param("sprintId") UUID sprintId,
                      @Param("status") TaskStatus status,
                      Pageable pageable);
    @Query("""
   SELECT t.sprint.id AS sprintId, COUNT(t) AS completedTaskCount
   FROM Task t
   WHERE t.status = :status
   GROUP BY t.sprint.id
   """)
    List<SprintTaskCount> countTasksBySprintAndStatus(@Param("status") TaskStatus status);

}

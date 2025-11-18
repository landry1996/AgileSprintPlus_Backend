package com.agilesprintplus.agilesprint.analytics.repo;


import com.agilesprintplus.agilesprint.analytics.entity.ForecastEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ForecastRepository extends JpaRepository<ForecastEntity, UUID> {
    List<ForecastEntity> findByMethodOrderByCreatedAtDesc(String method);
    List<ForecastEntity> findByUserId(UUID userId);
    List<ForecastEntity> findBySprintId(UUID sprintId);
}
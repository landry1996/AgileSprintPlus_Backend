package com.agilesprintplus.agilesprint.service;

import com.agilesprintplus.agilesprint.api.dto.SprintDtos;
import com.agilesprintplus.agilesprint.service.metriques.SprintTaskStatusCount;
import com.agilesprintplus.agilesprint.service.metriques.SprintVelocityView;
import org.springframework.data.domain.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
public interface SprintService {
    SprintDtos.Response create(SprintDtos.Create dto);
    SprintDtos.Response get(UUID id);
    SprintDtos.Response getByName(String name);

    Page<SprintDtos.Response> list(Pageable pageable);
    SprintDtos.Response update(UUID id, SprintDtos.Update dto);
    void delete(UUID id);
    Page<SprintDtos.Response> search(String keyword, Pageable pageable);
    List<SprintDtos.Response> listActive(LocalDate today);
    SprintDtos.Response getCurrent(LocalDate today);
    List<SprintDtos.Response> listOverlapping(LocalDate start, LocalDate end);
    List<SprintTaskStatusCount> countTasksByStatus(UUID sprintId);
    SprintVelocityView getVelocity(UUID sprintId);
}
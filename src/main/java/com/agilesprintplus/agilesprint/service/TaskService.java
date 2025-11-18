package com.agilesprintplus.agilesprint.service;

import com.agilesprintplus.agilesprint.api.dto.TaskDtos;
import com.agilesprintplus.agilesprint.domain.TaskStatus;
import com.agilesprintplus.agilesprint.service.taskcount.SprintTaskCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TaskService {

    TaskDtos.Response create(TaskDtos.Create dto);
    TaskDtos.Response get(UUID id);
    Page<TaskDtos.Response> list(Pageable pageable);
    TaskDtos.Response update(UUID id, TaskDtos.Update dto);
    void delete(UUID id);
    List<TaskDtos.Response> searchTask(String keyword);
    TaskDtos.Response assignUsersToTask(UUID taskId, Set<UUID> userIds);
    List<SprintTaskCount> getCountCompletedTasksBySprint();
    Long countByStatus(TaskStatus status);
}

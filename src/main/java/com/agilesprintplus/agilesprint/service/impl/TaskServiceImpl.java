package com.agilesprintplus.agilesprint.service.impl;

import com.agilesprintplus.agilesprint.api.dto.TaskDtos;
import com.agilesprintplus.agilesprint.domain.Sprint;
import com.agilesprintplus.agilesprint.domain.Task;
import com.agilesprintplus.agilesprint.domain.TaskStatus;
import com.agilesprintplus.agilesprint.domain.User;
import com.agilesprintplus.agilesprint.exception.BadRequestException;
import com.agilesprintplus.agilesprint.exception.NotFoundException;
import com.agilesprintplus.agilesprint.repo.SprintRepository;
import com.agilesprintplus.agilesprint.repo.TaskRepository;
import com.agilesprintplus.agilesprint.repo.UserRepository;
import com.agilesprintplus.agilesprint.service.TaskService;
import com.agilesprintplus.agilesprint.service.taskcount.SprintTaskCount;
import com.agilesprintplus.notification.events.TaskAssignedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

  private final TaskRepository taskRepo;
  private final SprintRepository sprintRepo;
  private final UserRepository userRepo;
  private final ApplicationEventPublisher events;

  @Override
  public TaskDtos.Response create(TaskDtos.Create dto) {
    Sprint sprint = null;
    if (dto.sprintId() != null) {
      sprint = sprintRepo.findById(dto.sprintId())
              .orElseThrow(() -> new NotFoundException("Sprint not found: " + dto.sprintId()));
    }

    Task task = Task.builder()
            .title(dto.title())
            .description(dto.description())
            .storyPoints(dto.storyPoints())
            .status(TaskStatus.TODO)
            .sprint(sprint)
            .build();

    return toResponse(taskRepo.save(task));
  }
  @Override
  @Transactional(readOnly = true)
  public TaskDtos.Response get(UUID id) {
    Task task = taskRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Task not found: " + id));
    return toResponse(task);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TaskDtos.Response> list(Pageable pageable) {
    return taskRepo.findAll(pageable).map(this::toResponse);
  }

  @Override
  public TaskDtos.Response update(UUID id, TaskDtos.Update dto) {
    Task task = taskRepo.findById(id)
            .orElseThrow(() -> new NotFoundException("Task not found: " + id));

    if (dto.title() != null)        task.setTitle(dto.title());
    if (dto.description() != null)  task.setDescription(dto.description());
    if (dto.status() != null)       task.setStatus(dto.status());
    if (dto.storyPoints() != null)  task.setStoryPoints(dto.storyPoints());

    if (dto.sprintId() != null) {
      Sprint sprint = sprintRepo.findById(dto.sprintId())
              .orElseThrow(() -> new NotFoundException("Sprint not found: " + dto.sprintId()));
      task.setSprint(sprint);
    }

    return toResponse(task);
  }
  @Override
  public void delete(UUID id) {
    if (!taskRepo.existsById(id)) {
      throw new NotFoundException("Task not found: " + id);
    }
    taskRepo.deleteById(id);
  }
  @Override
  @Transactional(readOnly = true)
  public List<TaskDtos.Response> searchTask(String keyword) {
    return taskRepo.search(keyword, null, null, Pageable.unpaged())
            .map(this::toResponse)
            .getContent();

  }
  @Override
  @Transactional(readOnly = true)
  public List<SprintTaskCount> getCountCompletedTasksBySprint() {
    return taskRepo.countTasksBySprintAndStatus(TaskStatus.DONE);
  }
  @Override
  @Transactional(readOnly = true)
  public Long countByStatus(TaskStatus status) {
    return taskRepo.count((root, query, cb) -> cb.equal(root.get("status"), status));
  }
  @Override
  public TaskDtos.Response assignUsersToTask(UUID taskId, Set<UUID> userIds) {
    Task task = taskRepo.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found: " + taskId));

    if (userIds == null || userIds.isEmpty()) {
      throw new BadRequestException("No userIds provided");
    }

    Set<User> users = new HashSet<>(userRepo.findAllById(userIds));
    if (users.isEmpty()) {
      throw new NotFoundException("No valid users found for assignment");
    }

    Set<UUID> foundIds = users.stream().map(User::getId).collect(Collectors.toSet());
    Set<UUID> missing = userIds.stream().filter(id -> !foundIds.contains(id)).collect(Collectors.toSet());
    if (!missing.isEmpty()) {
      throw new BadRequestException("Some user IDs were not found: " + missing);
    }

    if (task.getUsers() == null) task.setUsers(new HashSet<>());
    task.getUsers().addAll(users);

    task = taskRepo.save(task);
    events.publishEvent(new TaskAssignedEvent(task, users));

    return toResponse(task);
  }

  private TaskDtos.Response toResponse(Task task) {
    return new TaskDtos.Response(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getStoryPoints(),
            task.getSprint() != null ? task.getSprint().getId() : null,
            task.getUsers() == null ? Set.of()
                    : task.getUsers().stream().map(User::getId).collect(Collectors.toSet())
    );
  }
}

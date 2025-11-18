package com.agilesprintplus.agilesprint.api;
import com.agilesprintplus.agilesprint.api.dto.TaskDtos;
import com.agilesprintplus.agilesprint.domain.TaskStatus;
import com.agilesprintplus.agilesprint.service.TaskService;
import com.agilesprintplus.agilesprint.service.taskcount.SprintTaskCount;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin("*")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;

  @PreAuthorize("hasAuthority('task:create') or hasRole('ADMIN')")
  @PostMapping
  public ResponseEntity<TaskDtos.Response> create(@Valid @RequestBody TaskDtos.Create dto) {
    TaskDtos.Response response = taskService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PreAuthorize("hasAuthority('task:read') or hasRole('ADMIN')")
  @GetMapping("/{id}")
  public ResponseEntity<TaskDtos.Response> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(taskService.get(id));
  }

  @PreAuthorize("hasAuthority('task:read') or hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<Page<TaskDtos.Response>> list(Pageable pageable) {
    return ResponseEntity.ok(taskService.list(pageable));
  }

  @PreAuthorize("hasAuthority('task:update') or hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<TaskDtos.Response> update(@PathVariable("id") UUID id,
                                                  @Valid @RequestBody TaskDtos.Update dto) {
    return ResponseEntity.ok(taskService.update(id, dto));
  }

  @PreAuthorize("hasAuthority('task:delete') or hasRole('ADMIN')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    taskService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PreAuthorize("hasAuthority('task:read') or hasRole('ADMIN')")
  @GetMapping("/search")
  public ResponseEntity<List<TaskDtos.Response>> search(@RequestParam String keyword) {
    return ResponseEntity.ok(taskService.searchTask(keyword));
  }

  @PreAuthorize("hasAuthority('task:read') or hasRole('ADMIN')")
  @GetMapping("/status/{status}/count")
  public ResponseEntity<Long> countByStatus(@PathVariable TaskStatus status) {
    return ResponseEntity.ok(taskService.countByStatus(status));
  }

  @PreAuthorize("hasAuthority('task:read') or hasRole('ADMIN')")
  @GetMapping("/completed/by-sprint")
  public ResponseEntity<List<SprintTaskCount>> countCompletedTasksBySprint() {
    return ResponseEntity.ok(taskService.getCountCompletedTasksBySprint());
  }

  @PreAuthorize("hasAuthority('task:update') or hasRole('ADMIN')")
  @PutMapping("/{taskId}/assign-users")
  public ResponseEntity<TaskDtos.Response> assignUsersToTask(
          @PathVariable("taskId") UUID taskId,
          @RequestBody Set<UUID> userIds) {
    TaskDtos.Response response = taskService.assignUsersToTask(taskId, userIds);
    return ResponseEntity.ok(response);
  }
}

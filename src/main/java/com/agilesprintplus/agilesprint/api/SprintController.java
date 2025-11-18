package com.agilesprintplus.agilesprint.api;
import com.agilesprintplus.agilesprint.api.dto.SprintDtos;
import com.agilesprintplus.agilesprint.service.SprintService;
import com.agilesprintplus.agilesprint.service.metriques.SprintTaskStatusCount;
import com.agilesprintplus.agilesprint.service.metriques.SprintVelocityView;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sprints")
@CrossOrigin("*")
@RequiredArgsConstructor
public class SprintController {

  private final SprintService sprintService;

  // ------- ÉCRITURE (protégée) -------
  @PreAuthorize("hasAnyRole('ADMIN','PRODUCT_OWNER','SCRUM_MASTER') or hasAuthority('sprint:create')")
  @PostMapping
  public ResponseEntity<SprintDtos.Response> create(@Valid @RequestBody SprintDtos.Create dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(sprintService.create(dto));
  }

  @PreAuthorize("hasAnyRole('ADMIN','PRODUCT_OWNER','SCRUM_MASTER') or hasAuthority('sprint:update')")
  @PutMapping("/{id}")
  public ResponseEntity<SprintDtos.Response> update(@PathVariable("id") UUID id,
                                                    @Valid @RequestBody SprintDtos.Update dto) {
    return ResponseEntity.ok(sprintService.update(id, dto));
  }

  @PreAuthorize("hasAnyRole('ADMIN','PRODUCT_OWNER','SCRUM_MASTER') or hasAuthority('sprint:delete')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    sprintService.delete(id);
    return ResponseEntity.noContent().build();
  }

  // ------- LECTURE (ouverte à tout utilisateur authentifié) -------
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/{id}")
  public ResponseEntity<SprintDtos.Response> get(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(sprintService.get(id));
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping
  public ResponseEntity<Page<SprintDtos.Response>> list(Pageable pageable) {
    return ResponseEntity.ok(sprintService.list(pageable));
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/by-name/{name}")
  public ResponseEntity<SprintDtos.Response> getByName(@PathVariable String name) {
    return ResponseEntity.ok(sprintService.getByName(name));
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/search")
  public ResponseEntity<Page<SprintDtos.Response>> search(@RequestParam String keyword, Pageable pageable) {
    return ResponseEntity.ok(sprintService.search(keyword, pageable));
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/active")
  public ResponseEntity<List<SprintDtos.Response>> listActive(
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(sprintService.listActive(date));
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/current")
  public ResponseEntity<SprintDtos.Response> getCurrent(
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return ResponseEntity.ok(sprintService.getCurrent(date));
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/overlapping")
  public ResponseEntity<List<SprintDtos.Response>> listOverlapping(
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
    return ResponseEntity.ok(sprintService.listOverlapping(start, end));
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/{id}/tasks-status-count")
  public ResponseEntity<List<SprintTaskStatusCount>> countTasksByStatus(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(sprintService.countTasksByStatus(id));
  }

  @PreAuthorize("isAuthenticated()")
  @GetMapping("/{id}/velocity")
  public ResponseEntity<SprintVelocityView> getVelocity(@PathVariable("id") UUID id) {
    return ResponseEntity.ok(sprintService.getVelocity(id));
  }
}

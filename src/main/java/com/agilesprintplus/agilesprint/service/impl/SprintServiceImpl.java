package com.agilesprintplus.agilesprint.service.impl;

import com.agilesprintplus.agilesprint.api.dto.SprintDtos;
import com.agilesprintplus.agilesprint.domain.Sprint;
import com.agilesprintplus.agilesprint.exception.BadRequestException;
import com.agilesprintplus.agilesprint.exception.ConflictException;
import com.agilesprintplus.agilesprint.exception.NotFoundException;
import com.agilesprintplus.agilesprint.mapper.SprintMapper;
import com.agilesprintplus.agilesprint.repo.SprintRepository;
import com.agilesprintplus.agilesprint.repo.TaskRepository;
import com.agilesprintplus.agilesprint.service.SprintService;
import com.agilesprintplus.agilesprint.service.metriques.SprintTaskStatusCount;
import com.agilesprintplus.agilesprint.service.metriques.SprintVelocityView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Service
@Transactional
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

  private final SprintRepository repo;
  private final TaskRepository taskRepo;
  private final SprintMapper mapper;

  @Override
  public SprintDtos.Response create(SprintDtos.Create dto) {
    validateDates(dto.startDate(), dto.endDate());

    if (repo.existsByNameIgnoreCase(dto.name())) {
      throw new ConflictException("Sprint name already exists: " + dto.name());
    }
    ensureNoOverlaps(dto.startDate(), dto.endDate(), null);

    return toResponseWithCount(repo.save(mapper.toEntity(dto)));
  }
  @Override
  @Transactional(readOnly = true)
  public SprintDtos.Response get(UUID id) {
    Sprint s = repo.findById(id)
            .orElseThrow(() -> new NotFoundException("Sprint not found: " + id));
    return toResponseWithCount(s);
  }

  @Override
  @Transactional(readOnly = true)
  public SprintDtos.Response getByName(String name) {
    if (name == null || name.isBlank()) throw new BadRequestException("name is required");
    var sprint = repo.findByNameIgnoreCase(name)
            .orElseThrow(() -> new NotFoundException("Sprint not found with name: " + name));
    return toResponseWithCount(sprint);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<SprintDtos.Response> list(Pageable pageable) {
    return repo.findAll(pageable)
            .map(this::toResponseWithCount);
  }

  @Override
  public SprintDtos.Response update(UUID id, SprintDtos.Update dto) {
    Sprint s = repo.findById(id)
            .orElseThrow(() -> new NotFoundException("Sprint not found: " + id));
    if (dto.name() != null) {
      String newName = dto.name();
      if (!newName.equalsIgnoreCase(s.getName()) && repo.existsByNameIgnoreCase(newName)) {
        throw new ConflictException("Sprint name already exists: " + newName);
      }
    }

    mapper.updateEntity(s, dto);

    LocalDate start = s.getStartDate();
    LocalDate end = s.getEndDate();
    validateDates(start, end);
    ensureNoOverlaps(start, end, s.getId());
    s.setDurationDays(computeDurationDays(start, end));

    return toResponseWithCount(s);
  }
  @Override
  public void delete(UUID id) {
    if (!repo.existsById(id)) {
      throw new NotFoundException("Sprint not found: " + id);
    }
    repo.deleteById(id);
  }
  @Override
  @Transactional(readOnly = true)
  public Page<SprintDtos.Response> search(String keyword, Pageable pageable) {
    return repo.search(keyword, pageable).map(this::toResponseWithCount);
  }
  @Override
  @Transactional(readOnly = true)
  public List<SprintDtos.Response> listActive(LocalDate today) {
    LocalDate d = (today != null) ? today : LocalDate.now();
    return repo.findActive(d).stream().map(this::toResponseWithCount).toList();
  }
  @Override
  @Transactional(readOnly = true)
  public SprintDtos.Response getCurrent(LocalDate today) {
    LocalDate d = (today != null) ? today : LocalDate.now();
    Sprint s = repo.findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(d, d)
            .orElseThrow(() -> new NotFoundException("No current sprint for date: " + d));
    return toResponseWithCount(s);
  }
  @Override
  @Transactional(readOnly = true)
  public List<SprintDtos.Response> listOverlapping(LocalDate start, LocalDate end) {
    validateDates(start, end);
    return repo.findOverlapping(start, end).stream().map(this::toResponseWithCount).toList();
  }
  @Override
  @Transactional(readOnly = true)
  public List<SprintTaskStatusCount> countTasksByStatus(UUID sprintId) {
    if (!repo.existsById(sprintId)) {
      throw new NotFoundException("Sprint not found: " + sprintId);
    }
    return repo.countTasksByStatus(sprintId);
  }
  @Override
  @Transactional(readOnly = true)
  public SprintVelocityView getVelocity(UUID sprintId) {
    if (!repo.existsById(sprintId)) {
      throw new NotFoundException("Sprint not found: " + sprintId);
    }
    return repo.getVelocity(sprintId)
            .orElseThrow(() -> new NotFoundException("No velocity data for sprint: " + sprintId));
  }
  private void validateDates(LocalDate start, LocalDate end) {
    if (start == null || end == null) {
      throw new BadRequestException("startDate and endDate are required");
    }
    if (end.isBefore(start)) {
      throw new BadRequestException("endDate must be on or after startDate");
    }
  }
  private void ensureNoOverlaps(LocalDate start, LocalDate end, UUID excludeId) {
    var overlaps = repo.findOverlapping(start, end);
    boolean hasOverlap = overlaps.stream()
            .anyMatch(s -> excludeId == null || !Objects.equals(s.getId(), excludeId));
    if (hasOverlap) {
      throw new ConflictException("Sprint dates overlap with existing sprint(s)");
    }
  }
  private int computeDurationDays(LocalDate start, LocalDate end) {
    long days = ChronoUnit.DAYS.between(start, end) + 1;
    return (int) Math.max(days, 0);
  }
  private SprintDtos.Response toResponseWithCount(Sprint s) {
    long count = taskRepo.countBySprint_Id(s.getId());
    return new SprintDtos.Response(
            s.getId(),
            s.getName(),
            s.getStartDate(),
            s.getEndDate(),
            s.getDurationDays(),
            s.getGoal(),
            count
    );
    // Variante: on pourrait ajouter au mapper une méthode dédiée toResponse(Sprint, @Context long count)
    // mais créer directement le record ici est simple et évite toute charge LAZY.
  }
}

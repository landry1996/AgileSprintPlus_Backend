package com.agilesprintplus.agilesprint.mapper;

import com.agilesprintplus.agilesprint.api.dto.SprintDtos;
import com.agilesprintplus.agilesprint.domain.Sprint;
import com.agilesprintplus.agilesprint.exception.BadRequestException;
import org.mapstruct.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SprintMapper {
    @BeforeMapping
    default void validateCreate(SprintDtos.Create dto) {
        requireNonNull(dto.startDate(), "startDate is required");
        requireNonNull(dto.endDate(), "endDate is required");
        validateChronology(dto.startDate(), dto.endDate());
    }
    @BeforeMapping
    default void validateUpdate(SprintDtos.Update dto, @MappingTarget Sprint ignored) {
        if (dto.startDate() != null && dto.endDate() != null) {
            validateChronology(dto.startDate(), dto.endDate());
        }
    }
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "durationDays", ignore = true)
    Sprint toEntity(SprintDtos.Create dto);
    @BeanMapping(ignoreByDefault = true, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "startDate", source = "startDate"),
            @Mapping(target = "endDate", source = "endDate"),
            @Mapping(target = "goal", source = "goal")
    })
    void updateEntity(@MappingTarget Sprint sprint, SprintDtos.Update dto);
    @Mapping(target = "tasksCount",
            expression = "java(sprint.getTasks() != null ? sprint.getTasks().size() : 0L)")
    SprintDtos.Response toResponse(Sprint sprint);
    @AfterMapping
    default void afterCreate(SprintDtos.Create dto, @MappingTarget Sprint sprint) {
        setDurationDaysFromDates(sprint, sprint.getStartDate(), sprint.getEndDate());
    }
    @AfterMapping
    default void afterUpdate(SprintDtos.Update dto, @MappingTarget Sprint sprint) {
        if (sprint.getStartDate() != null && sprint.getEndDate() != null) {
            validateChronology(sprint.getStartDate(), sprint.getEndDate());
            setDurationDaysFromDates(sprint, sprint.getStartDate(), sprint.getEndDate());
        }
    }
    private static void validateChronology(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) throw new BadRequestException("endDate must be on or after startDate");
    }
    private static void setDurationDaysFromDates(Sprint sprint, LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        sprint.setDurationDays((int) Math.max(days, 0));
    }
    private static <T> T requireNonNull(T value, String message) {
        if (value == null) throw new BadRequestException(message);
        return value;
    }
}

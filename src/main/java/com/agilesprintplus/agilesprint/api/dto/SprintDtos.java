package com.agilesprintplus.agilesprint.api.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;
public class SprintDtos {

    @Schema(name = "SprintCreate")
    public record Create(
            @NotBlank
            @Schema(example = "Sprint 1")
            String name,

            @NotNull
            @Schema(type = "string", format = "date", example = "2025-11-03")
            LocalDate startDate,

            @NotNull
            @Schema(type = "string", format = "date", example = "2025-11-09")
            LocalDate endDate,

            @Size(max = 255)
            @Schema(example = "Stabiliser l’authentification et livrer le backlog A1")
            String goal
    ) {}

  public record Update(
          String name,
          LocalDate startDate,
          LocalDate endDate,
          String goal
  ) {}
  public record Response(
          UUID id,
          String name,
          LocalDate startDate,
          LocalDate endDate,
          Integer durationDays,
          String goal,
          long tasksCount
  ) {}
}

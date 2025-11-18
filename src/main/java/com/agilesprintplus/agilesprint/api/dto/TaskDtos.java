package com.agilesprintplus.agilesprint.api.dto;

import com.agilesprintplus.agilesprint.domain.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.UUID;
public class TaskDtos {

    @Schema(name = "TaskCreate")
    public record Create(
            @NotBlank @Schema(example = "Implement login form") String title,
            @Schema(example = "Form + validation") String description,
            @Schema(example = "3") Integer storyPoints,
            @Schema(description = "Optional sprint id", example = "2d7d8bdf-4b7a-42a3-9a3a-0e2c2c1f8c77")
            UUID sprintId
    ) {}


    public record Update(
            String title,
            String description,
            TaskStatus status,
            Integer storyPoints,
            UUID sprintId
    ) {}

    public record Response(
            UUID id,
            String title,
            String description,
            TaskStatus status,
            Integer storyPoints,
            UUID sprintId,
            Set<UUID> userIds
    ) {}

}

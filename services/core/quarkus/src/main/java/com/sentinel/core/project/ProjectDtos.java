package com.sentinel.core.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class ProjectDtos {
    private ProjectDtos() {
    }

    public record CreateProject(@NotBlank @Size(max = 120) String name) {
    }

    public record ProjectResponse(UUID id, String name, Instant createdAt) {
        static ProjectResponse from(Project project) {
            return new ProjectResponse(project.id, project.name, project.createdAt);
        }
    }
}
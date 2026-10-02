package com.sentinel.core.adapter.in.rest.project;

import com.sentinel.core.domain.common.PageResult;
import com.sentinel.core.domain.project.Project;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ProjectDtos {
    private ProjectDtos() {
    }

    @Schema(name = "ProjectResponse", description = "Dados resumidos de um projeto")
    public record ProjectResponse(
            @Schema(description = "Identificador único do projeto", required = true)
            UUID id,
            @Schema(description = "Nome do projeto", required = true, example = "Sentinel")
            String name,
            @Schema(description = "Data e hora de criação", required = true)
            Instant createdAt
    ) {
        public static ProjectResponse from(Project project) {
            return new ProjectResponse(project.getId(), project.getName(), project.getCreatedAt());
        }
    }

    @Schema(name = "ProjectPageResponse", description = "Resposta paginada de projetos")
    public record ProjectPageResponse(
            List<ProjectResponse> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        public static ProjectPageResponse from(PageResult<Project> pageResult) {
            List<ProjectResponse> dtos = pageResult.items().stream()
                    .map(ProjectResponse::from)
                    .toList();
            return new ProjectPageResponse(
                    dtos,
                    pageResult.page(),
                    pageResult.size(),
                    pageResult.totalElements(),
                    pageResult.totalPages()
            );
        }
    }
}

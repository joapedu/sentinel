package com.sentinel.core.adapter.in.rest.project;

import com.sentinel.core.application.project.GetProjectUseCase;
import com.sentinel.core.application.project.ListProjectsUseCase;
import com.sentinel.core.auth.InvalidCredentialsException;
import com.sentinel.core.domain.common.PageResult;
import com.sentinel.core.domain.project.Project;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/projects")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces({MediaType.APPLICATION_JSON, "application/problem+json"})
@Tag(name = "Projects", description = "Operações de consulta de projetos do usuário autenticado")
public class ProjectResource {

    @Inject
    JsonWebToken token;

    @Inject
    ListProjectsUseCase listProjectsUseCase;

    @Inject
    GetProjectUseCase getProjectUseCase;

    @GET
    @Operation(summary = "Listar projetos", description = "Retorna lista paginada dos projetos pertencentes ao usuário autenticado, com filtro opcional por nome.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Lista paginada de projetos retornada com sucesso"),
            @APIResponse(responseCode = "401", description = "Token JWT ausente ou inválido")
    })
    public ProjectDtos.ProjectPageResponse list(
            @Parameter(description = "Número da página (0-indexed)") @QueryParam("page") @DefaultValue("0") int page,
            @Parameter(description = "Tamanho da página (máx. 100)") @QueryParam("size") @DefaultValue("10") int size,
            @Parameter(description = "Filtro parcial pelo nome do projeto") @QueryParam("name") String name
    ) {
        UUID userId = currentUserId();
        PageResult<Project> result = listProjectsUseCase.execute(userId, name, page, size);
        return ProjectDtos.ProjectPageResponse.from(result);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Buscar projeto por ID", description = "Retorna os detalhes de um projeto específico pertencente ao usuário autenticado.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Projeto encontrado com sucesso"),
            @APIResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @APIResponse(responseCode = "404", description = "Projeto não encontrado para este usuário")
    })
    public ProjectDtos.ProjectResponse getById(@Parameter(description = "ID do projeto") @PathParam("id") UUID id) {
        UUID userId = currentUserId();
        Project project = getProjectUseCase.execute(id, userId);
        return ProjectDtos.ProjectResponse.from(project);
    }

    private UUID currentUserId() {
        String subject = token != null ? token.getSubject() : null;
        if (subject == null || subject.isBlank()) {
            throw new InvalidCredentialsException();
        }
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new InvalidCredentialsException();
        }
    }
}

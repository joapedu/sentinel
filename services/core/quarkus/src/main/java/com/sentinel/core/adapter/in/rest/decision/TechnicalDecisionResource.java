package com.sentinel.core.adapter.in.rest.decision;

import com.sentinel.core.application.decision.CreateDecisionUseCase;
import com.sentinel.core.application.decision.GetDecisionUseCase;
import com.sentinel.core.application.decision.ListDecisionsUseCase;
import com.sentinel.core.auth.InvalidCredentialsException;
import com.sentinel.core.domain.common.PageResult;
import com.sentinel.core.domain.decision.DecisionStatus;
import com.sentinel.core.domain.decision.TechnicalDecision;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/api/v1/projects/{projectId}/decisions")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces({MediaType.APPLICATION_JSON, "application/problem+json"})
@Tag(name = "Technical Decisions", description = "Operações com decisões técnicas associadas a um projeto")
public class TechnicalDecisionResource {

    @Inject
    JsonWebToken token;

    @Inject
    CreateDecisionUseCase createDecisionUseCase;

    @Inject
    ListDecisionsUseCase listDecisionsUseCase;

    @Inject
    GetDecisionUseCase getDecisionUseCase;

    @Context
    UriInfo uriInfo;

    @POST
    @Operation(summary = "Criar decisão técnica", description = "Registra uma nova decisão técnica vinculada a um projeto específico pertencente ao usuário.")
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Decisão técnica criada com sucesso",
                    headers = @Header(name = "Location", description = "URI do recurso criado")
            ),
            @APIResponse(responseCode = "400", description = "Dados da requisição inválidos (RFC 9457 Problem Details)"),
            @APIResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @APIResponse(responseCode = "404", description = "Projeto não encontrado para o usuário autenticado")
    })
    public Response create(
            @Parameter(description = "ID do projeto", required = true) @PathParam("projectId") UUID projectId,
            @Valid @NotNull TechnicalDecisionDtos.CreateTechnicalDecisionRequest request
    ) {
        UUID userId = currentUserId();
        TechnicalDecision decision = createDecisionUseCase.execute(
                projectId,
                userId,
                request.title(),
                request.description(),
                request.status(),
                request.author(),
                request.decisionDate()
        );

        URI location = uriInfo.getBaseUriBuilder()
                .path("api/v1/projects")
                .path(projectId.toString())
                .path("decisions")
                .path(decision.getId().toString())
                .build();

        return Response.created(location)
                .entity(TechnicalDecisionDtos.TechnicalDecisionResponse.from(decision))
                .build();
    }

    @GET
    @Operation(summary = "Listar decisões técnicas", description = "Retorna lista paginada das decisões técnicas de um projeto com filtros opcionais por status e título.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Lista paginada de decisões retornada com sucesso"),
            @APIResponse(responseCode = "400", description = "Filtro inválido"),
            @APIResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @APIResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public TechnicalDecisionDtos.TechnicalDecisionPageResponse list(
            @Parameter(description = "ID do projeto", required = true) @PathParam("projectId") UUID projectId,
            @Parameter(description = "Número da página (0-indexed)") @QueryParam("page") @DefaultValue("0") int page,
            @Parameter(description = "Tamanho da página (máx. 100)") @QueryParam("size") @DefaultValue("10") int size,
            @Parameter(description = "Filtro por status (PROPOSED, ACCEPTED, DEPRECATED, REJECTED)") @QueryParam("status") String status,
            @Parameter(description = "Filtro parcial por texto no título") @QueryParam("title") String title
    ) {
        UUID userId = currentUserId();
        DecisionStatus statusEnum = DecisionStatus.fromString(status);
        PageResult<TechnicalDecision> result = listDecisionsUseCase.execute(
                projectId,
                userId,
                statusEnum,
                title,
                page,
                size
        );
        return TechnicalDecisionDtos.TechnicalDecisionPageResponse.from(result);
    }

    @GET
    @Path("/{decisionId}")
    @Operation(summary = "Buscar decisão técnica por ID", description = "Retorna os detalhes de uma decisão técnica específica vinculada ao projeto informado.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Decisão técnica encontrada"),
            @APIResponse(responseCode = "401", description = "Token JWT ausente ou inválido"),
            @APIResponse(responseCode = "404", description = "Projeto ou decisão não encontrada")
    })
    public TechnicalDecisionDtos.TechnicalDecisionResponse getById(
            @Parameter(description = "ID do projeto", required = true) @PathParam("projectId") UUID projectId,
            @Parameter(description = "ID da decisão técnica", required = true) @PathParam("decisionId") UUID decisionId
    ) {
        UUID userId = currentUserId();
        TechnicalDecision decision = getDecisionUseCase.execute(projectId, decisionId, userId);
        return TechnicalDecisionDtos.TechnicalDecisionResponse.from(decision);
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

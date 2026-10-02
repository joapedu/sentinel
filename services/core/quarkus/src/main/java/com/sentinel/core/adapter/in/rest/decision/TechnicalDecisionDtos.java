package com.sentinel.core.adapter.in.rest.decision;

import com.sentinel.core.domain.common.PageResult;
import com.sentinel.core.domain.decision.DecisionStatus;
import com.sentinel.core.domain.decision.TechnicalDecision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class TechnicalDecisionDtos {
    private TechnicalDecisionDtos() {
    }

    @Schema(name = "CreateTechnicalDecisionRequest", description = "Dados para criação de uma decisão técnica")
    public record CreateTechnicalDecisionRequest(
            @Schema(description = "Título da decisão", required = true, example = "Utilizar PostgreSQL no módulo de pagamentos")
            @NotBlank(message = "O título não pode estar em branco")
            @Size(max = 200, message = "O título deve ter no máximo 200 caracteres")
            String title,

            @Schema(description = "Descrição e justificativa técnica detalhada", required = true, example = "Necessidade de suporte a transações ACID e robustez relacional.")
            @NotBlank(message = "A descrição não pode estar em branco")
            String description,

            @Schema(description = "Status da decisão", required = true, example = "ACCEPTED", enumeration = {"PROPOSED", "ACCEPTED", "DEPRECATED", "REJECTED"})
            @NotNull(message = "O status é obrigatório")
            DecisionStatus status,

            @Schema(description = "Autor ou responsável pela decisão", required = true, example = "Lucas Mangabeira")
            @NotBlank(message = "O autor não pode estar em branco")
            @Size(max = 120, message = "O autor deve ter no máximo 120 caracteres")
            String author,

            @Schema(description = "Data em que a decisão foi tomada", required = true, example = "2026-10-02")
            @NotNull(message = "A data da decisão é obrigatória")
            LocalDate decisionDate
    ) {
    }

    @Schema(name = "TechnicalDecisionResponse", description = "Representação detalhada de uma decisão técnica")
    public record TechnicalDecisionResponse(
            UUID id,
            UUID projectId,
            String title,
            String description,
            DecisionStatus status,
            String author,
            LocalDate decisionDate,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static TechnicalDecisionResponse from(TechnicalDecision decision) {
            return new TechnicalDecisionResponse(
                    decision.getId(),
                    decision.getProjectId(),
                    decision.getTitle(),
                    decision.getDescription(),
                    decision.getStatus(),
                    decision.getAuthor(),
                    decision.getDecisionDate(),
                    decision.getCreatedAt(),
                    decision.getUpdatedAt()
            );
        }
    }

    @Schema(name = "TechnicalDecisionPageResponse", description = "Resposta paginada de decisões técnicas")
    public record TechnicalDecisionPageResponse(
            List<TechnicalDecisionResponse> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
        public static TechnicalDecisionPageResponse from(PageResult<TechnicalDecision> pageResult) {
            List<TechnicalDecisionResponse> dtos = pageResult.items().stream()
                    .map(TechnicalDecisionResponse::from)
                    .toList();
            return new TechnicalDecisionPageResponse(
                    dtos,
                    pageResult.page(),
                    pageResult.size(),
                    pageResult.totalElements(),
                    pageResult.totalPages()
            );
        }
    }
}

package com.sentinel.core.application.decision;

import com.sentinel.core.domain.common.exception.ResourceNotFoundException;
import com.sentinel.core.domain.decision.TechnicalDecision;
import com.sentinel.core.domain.decision.TechnicalDecisionRepository;
import com.sentinel.core.domain.project.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class GetDecisionUseCase {
    private final ProjectRepository projectRepository;
    private final TechnicalDecisionRepository decisionRepository;

    @Inject
    public GetDecisionUseCase(
            ProjectRepository projectRepository,
            TechnicalDecisionRepository decisionRepository
    ) {
        this.projectRepository = projectRepository;
        this.decisionRepository = decisionRepository;
    }

    public TechnicalDecision execute(UUID projectId, UUID decisionId, UUID ownerId) {
        projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id: " + projectId));

        return decisionRepository.findByIdAndProjectId(decisionId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Decisão técnica não encontrada com id: " + decisionId));
    }
}

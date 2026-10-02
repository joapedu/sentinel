package com.sentinel.core.application.decision;

import com.sentinel.core.domain.common.PageResult;
import com.sentinel.core.domain.common.exception.ResourceNotFoundException;
import com.sentinel.core.domain.decision.DecisionStatus;
import com.sentinel.core.domain.decision.TechnicalDecision;
import com.sentinel.core.domain.decision.TechnicalDecisionRepository;
import com.sentinel.core.domain.project.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class ListDecisionsUseCase {
    private final ProjectRepository projectRepository;
    private final TechnicalDecisionRepository decisionRepository;

    @Inject
    public ListDecisionsUseCase(
            ProjectRepository projectRepository,
            TechnicalDecisionRepository decisionRepository
    ) {
        this.projectRepository = projectRepository;
        this.decisionRepository = decisionRepository;
    }

    public PageResult<TechnicalDecision> execute(
            UUID projectId,
            UUID ownerId,
            DecisionStatus statusFilter,
            String titleFilter,
            int page,
            int size
    ) {
        projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id: " + projectId));

        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = (size <= 0 || size > 100) ? 10 : size;
        return decisionRepository.findByProjectIdPaged(projectId, statusFilter, titleFilter, sanitizedPage, sanitizedSize);
    }
}

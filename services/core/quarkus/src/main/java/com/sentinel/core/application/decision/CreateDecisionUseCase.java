package com.sentinel.core.application.decision;

import com.sentinel.core.domain.common.exception.ResourceNotFoundException;
import com.sentinel.core.domain.decision.DecisionStatus;
import com.sentinel.core.domain.decision.TechnicalDecision;
import com.sentinel.core.domain.decision.TechnicalDecisionRepository;
import com.sentinel.core.domain.project.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;

@ApplicationScoped
public class CreateDecisionUseCase {
    private final ProjectRepository projectRepository;
    private final TechnicalDecisionRepository decisionRepository;

    @Inject
    public CreateDecisionUseCase(
            ProjectRepository projectRepository,
            TechnicalDecisionRepository decisionRepository
    ) {
        this.projectRepository = projectRepository;
        this.decisionRepository = decisionRepository;
    }

    public TechnicalDecision execute(
            UUID projectId,
            UUID ownerId,
            String title,
            String description,
            DecisionStatus status,
            String author,
            LocalDate decisionDate
    ) {
        projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id: " + projectId));

        TechnicalDecision decision = TechnicalDecision.create(
                projectId,
                title.trim(),
                description.trim(),
                status,
                author.trim(),
                decisionDate
        );

        return decisionRepository.save(decision);
    }
}

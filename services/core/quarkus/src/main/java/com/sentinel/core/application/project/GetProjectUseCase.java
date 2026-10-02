package com.sentinel.core.application.project;

import com.sentinel.core.domain.common.exception.ResourceNotFoundException;
import com.sentinel.core.domain.project.Project;
import com.sentinel.core.domain.project.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class GetProjectUseCase {
    private final ProjectRepository projectRepository;

    @Inject
    public GetProjectUseCase(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Project execute(UUID projectId, UUID ownerId) {
        return projectRepository.findByIdAndOwnerId(projectId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com id: " + projectId));
    }
}

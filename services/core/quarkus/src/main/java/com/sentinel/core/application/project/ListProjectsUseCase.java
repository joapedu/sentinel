package com.sentinel.core.application.project;

import com.sentinel.core.domain.common.PageResult;
import com.sentinel.core.domain.project.Project;
import com.sentinel.core.domain.project.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class ListProjectsUseCase {
    private final ProjectRepository projectRepository;

    @Inject
    public ListProjectsUseCase(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public PageResult<Project> execute(UUID ownerId, String nameFilter, int page, int size) {
        int sanitizedPage = Math.max(0, page);
        int sanitizedSize = (size <= 0 || size > 100) ? 10 : size;
        return projectRepository.findByOwnerIdPaged(ownerId, nameFilter, sanitizedPage, sanitizedSize);
    }
}

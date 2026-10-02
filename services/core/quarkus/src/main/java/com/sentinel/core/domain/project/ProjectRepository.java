package com.sentinel.core.domain.project;

import com.sentinel.core.domain.common.PageResult;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository {
    Optional<Project> findById(UUID id);
    Optional<Project> findByIdAndOwnerId(UUID id, UUID ownerId);
    PageResult<Project> findByOwnerIdPaged(UUID ownerId, String nameFilter, int page, int size);
    Project save(Project project);
    boolean existsById(UUID id);
}

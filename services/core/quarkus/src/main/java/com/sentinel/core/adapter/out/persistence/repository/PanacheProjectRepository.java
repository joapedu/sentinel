package com.sentinel.core.adapter.out.persistence.repository;

import com.sentinel.core.adapter.out.persistence.entity.ProjectJpaEntity;
import com.sentinel.core.auth.User;
import com.sentinel.core.domain.common.PageResult;
import com.sentinel.core.domain.project.Project;
import com.sentinel.core.domain.project.ProjectRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheProjectRepository implements ProjectRepository {

    private final ProjectPanacheRepository panacheRepo;

    @Inject
    public PanacheProjectRepository(ProjectPanacheRepository panacheRepo) {
        this.panacheRepo = panacheRepo;
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return panacheRepo.findByIdOptional(id).map(ProjectJpaEntity::toDomain);
    }

    @Override
    public Optional<Project> findByIdAndOwnerId(UUID id, UUID ownerId) {
        return panacheRepo.find("id = ?1 and owner.id = ?2", id, ownerId)
                .firstResultOptional()
                .map(ProjectJpaEntity::toDomain);
    }

    @Override
    public PageResult<Project> findByOwnerIdPaged(UUID ownerId, String nameFilter, int page, int size) {
        PanacheQuery<ProjectJpaEntity> query;
        if (nameFilter != null && !nameFilter.isBlank()) {
            query = panacheRepo.find("owner.id = ?1 and lower(name) like ?2 order by createdAt desc",
                    ownerId, "%" + nameFilter.trim().toLowerCase() + "%");
        } else {
            query = panacheRepo.find("owner.id = ?1 order by createdAt desc", ownerId);
        }

        long total = query.count();
        List<Project> items = query.page(Page.of(page, size))
                .list()
                .stream()
                .map(ProjectJpaEntity::toDomain)
                .toList();

        return PageResult.of(items, page, size, total);
    }

    @Override
    @Transactional
    public Project save(Project project) {
        User owner = User.findById(project.getOwnerId());
        if (owner == null) {
            throw new IllegalArgumentException("Usuário proprietário não encontrado: " + project.getOwnerId());
        }

        ProjectJpaEntity entity = panacheRepo.findByIdOptional(project.getId()).orElse(null);
        if (entity == null) {
            entity = ProjectJpaEntity.fromDomain(project, owner);
            panacheRepo.persist(entity);
        } else {
            entity.name = project.getName();
            panacheRepo.persist(entity);
        }
        return entity.toDomain();
    }

    @Override
    public boolean existsById(UUID id) {
        return panacheRepo.count("id", id) > 0;
    }
}

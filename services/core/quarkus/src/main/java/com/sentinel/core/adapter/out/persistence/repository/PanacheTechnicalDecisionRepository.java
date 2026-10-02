package com.sentinel.core.adapter.out.persistence.repository;

import com.sentinel.core.adapter.out.persistence.entity.ProjectJpaEntity;
import com.sentinel.core.adapter.out.persistence.entity.TechnicalDecisionJpaEntity;
import com.sentinel.core.domain.common.PageResult;
import com.sentinel.core.domain.decision.DecisionStatus;
import com.sentinel.core.domain.decision.TechnicalDecision;
import com.sentinel.core.domain.decision.TechnicalDecisionRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class PanacheTechnicalDecisionRepository implements TechnicalDecisionRepository {

    private final TechnicalDecisionPanacheRepository decisionPanacheRepo;
    private final ProjectPanacheRepository projectPanacheRepo;

    @Inject
    public PanacheTechnicalDecisionRepository(
            TechnicalDecisionPanacheRepository decisionPanacheRepo,
            ProjectPanacheRepository projectPanacheRepo
    ) {
        this.decisionPanacheRepo = decisionPanacheRepo;
        this.projectPanacheRepo = projectPanacheRepo;
    }

    @Override
    @Transactional
    public TechnicalDecision save(TechnicalDecision decision) {
        ProjectJpaEntity project = projectPanacheRepo.findByIdOptional(decision.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Projeto não encontrado com id: " + decision.getProjectId()));

        TechnicalDecisionJpaEntity entity = decisionPanacheRepo.findByIdOptional(decision.getId()).orElse(null);
        if (entity == null) {
            entity = TechnicalDecisionJpaEntity.fromDomain(decision, project);
            decisionPanacheRepo.persist(entity);
        } else {
            entity.title = decision.getTitle();
            entity.description = decision.getDescription();
            entity.status = decision.getStatus().name();
            entity.author = decision.getAuthor();
            entity.decisionDate = decision.getDecisionDate();
            entity.updatedAt = decision.getUpdatedAt();
            decisionPanacheRepo.persist(entity);
        }
        return entity.toDomain();
    }

    @Override
    public Optional<TechnicalDecision> findById(UUID id) {
        return decisionPanacheRepo.findByIdOptional(id).map(TechnicalDecisionJpaEntity::toDomain);
    }

    @Override
    public Optional<TechnicalDecision> findByIdAndProjectId(UUID id, UUID projectId) {
        return decisionPanacheRepo.find("id = ?1 and project.id = ?2", id, projectId)
                .firstResultOptional()
                .map(TechnicalDecisionJpaEntity::toDomain);
    }

    @Override
    public PageResult<TechnicalDecision> findByProjectIdPaged(
            UUID projectId,
            DecisionStatus statusFilter,
            String titleFilter,
            int page,
            int size
    ) {
        StringBuilder query = new StringBuilder("project.id = :projectId");
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);

        if (statusFilter != null) {
            query.append(" and status = :status");
            params.put("status", statusFilter.name());
        }

        if (titleFilter != null && !titleFilter.isBlank()) {
            query.append(" and lower(title) like :title");
            params.put("title", "%" + titleFilter.trim().toLowerCase() + "%");
        }

        query.append(" order by decisionDate desc, createdAt desc");

        PanacheQuery<TechnicalDecisionJpaEntity> panacheQuery = decisionPanacheRepo.find(query.toString(), params);
        long total = panacheQuery.count();
        List<TechnicalDecision> items = panacheQuery.page(Page.of(page, size))
                .list()
                .stream()
                .map(TechnicalDecisionJpaEntity::toDomain)
                .toList();

        return PageResult.of(items, page, size, total);
    }
}

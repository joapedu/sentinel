package com.sentinel.core.domain.decision;

import com.sentinel.core.domain.common.PageResult;
import java.util.Optional;
import java.util.UUID;

public interface TechnicalDecisionRepository {
    TechnicalDecision save(TechnicalDecision decision);
    Optional<TechnicalDecision> findById(UUID id);
    Optional<TechnicalDecision> findByIdAndProjectId(UUID id, UUID projectId);
    PageResult<TechnicalDecision> findByProjectIdPaged(UUID projectId, DecisionStatus statusFilter, String titleFilter, int page, int size);
}

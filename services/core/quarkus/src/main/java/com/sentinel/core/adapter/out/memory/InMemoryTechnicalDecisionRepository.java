package com.sentinel.core.adapter.out.memory;

import com.sentinel.core.domain.common.PageResult;
import com.sentinel.core.domain.decision.DecisionStatus;
import com.sentinel.core.domain.decision.TechnicalDecision;
import com.sentinel.core.domain.decision.TechnicalDecisionRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTechnicalDecisionRepository implements TechnicalDecisionRepository {
    private final Map<UUID, TechnicalDecision> store = new ConcurrentHashMap<>();

    @Override
    public TechnicalDecision save(TechnicalDecision decision) {
        store.put(decision.getId(), decision);
        return decision;
    }

    @Override
    public Optional<TechnicalDecision> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<TechnicalDecision> findByIdAndProjectId(UUID id, UUID projectId) {
        return Optional.ofNullable(store.get(id))
                .filter(d -> d.getProjectId().equals(projectId));
    }

    @Override
    public PageResult<TechnicalDecision> findByProjectIdPaged(
            UUID projectId,
            DecisionStatus statusFilter,
            String titleFilter,
            int page,
            int size
    ) {
        List<TechnicalDecision> filtered = store.values().stream()
                .filter(d -> d.getProjectId().equals(projectId))
                .filter(d -> statusFilter == null || d.getStatus() == statusFilter)
                .filter(d -> titleFilter == null || titleFilter.isBlank() || d.getTitle().toLowerCase().contains(titleFilter.trim().toLowerCase()))
                .sorted((a, b) -> {
                    int dateComp = b.getDecisionDate().compareTo(a.getDecisionDate());
                    if (dateComp != 0) return dateComp;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .toList();

        long total = filtered.size();
        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<TechnicalDecision> pageItems = filtered.subList(fromIndex, toIndex);

        return PageResult.of(pageItems, page, size, total);
    }

    public void clear() {
        store.clear();
    }
}

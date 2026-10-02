package com.sentinel.core.adapter.out.memory;

import com.sentinel.core.domain.common.PageResult;
import com.sentinel.core.domain.project.Project;
import com.sentinel.core.domain.project.ProjectRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProjectRepository implements ProjectRepository {
    private final Map<UUID, Project> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Project> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Project> findByIdAndOwnerId(UUID id, UUID ownerId) {
        return Optional.ofNullable(store.get(id))
                .filter(p -> p.getOwnerId().equals(ownerId));
    }

    @Override
    public PageResult<Project> findByOwnerIdPaged(UUID ownerId, String nameFilter, int page, int size) {
        List<Project> filtered = store.values().stream()
                .filter(p -> p.getOwnerId().equals(ownerId))
                .filter(p -> nameFilter == null || nameFilter.isBlank() || p.getName().toLowerCase().contains(nameFilter.trim().toLowerCase()))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        long total = filtered.size();
        int fromIndex = Math.min(page * size, filtered.size());
        int toIndex = Math.min(fromIndex + size, filtered.size());
        List<Project> pageItems = filtered.subList(fromIndex, toIndex);

        return PageResult.of(pageItems, page, size, total);
    }

    @Override
    public Project save(Project project) {
        store.put(project.getId(), project);
        return project;
    }

    @Override
    public boolean existsById(UUID id) {
        return store.containsKey(id);
    }

    public void clear() {
        store.clear();
    }
}

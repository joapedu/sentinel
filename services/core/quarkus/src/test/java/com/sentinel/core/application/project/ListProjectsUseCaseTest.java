package com.sentinel.core.application.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sentinel.core.adapter.out.memory.InMemoryProjectRepository;
import com.sentinel.core.domain.common.PageResult;
import com.sentinel.core.domain.project.Project;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListProjectsUseCaseTest {

    private InMemoryProjectRepository repository;
    private ListProjectsUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProjectRepository();
        useCase = new ListProjectsUseCase(repository);
    }

    @Test
    void listsProjectsWithPaginationAndFiltering() {
        UUID ownerId = UUID.randomUUID();
        UUID otherOwner = UUID.randomUUID();

        repository.save(Project.create("Sentinel Core", ownerId));
        repository.save(Project.create("Sentinel Ingestion", ownerId));
        repository.save(Project.create("Other Project", otherOwner));

        PageResult<Project> allUserProjects = useCase.execute(ownerId, null, 0, 10);
        assertEquals(2, allUserProjects.items().size());
        assertEquals(2, allUserProjects.totalElements());

        PageResult<Project> filtered = useCase.execute(ownerId, "core", 0, 10);
        assertEquals(1, filtered.items().size());
        assertEquals("Sentinel Core", filtered.items().get(0).getName());

        PageResult<Project> emptyPage = useCase.execute(ownerId, "inexistente", 0, 10);
        assertTrue(emptyPage.items().isEmpty());
    }
}

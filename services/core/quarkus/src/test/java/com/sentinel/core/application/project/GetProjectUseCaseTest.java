package com.sentinel.core.application.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sentinel.core.adapter.out.memory.InMemoryProjectRepository;
import com.sentinel.core.domain.common.exception.ResourceNotFoundException;
import com.sentinel.core.domain.project.Project;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetProjectUseCaseTest {

    private InMemoryProjectRepository repository;
    private GetProjectUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProjectRepository();
        useCase = new GetProjectUseCase(repository);
    }

    @Test
    void findsProjectByOwnerAndId() {
        UUID ownerId = UUID.randomUUID();
        Project saved = repository.save(Project.create("Sentinel Core", ownerId));

        Project found = useCase.execute(saved.getId(), ownerId);
        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    void throwsNotFoundWhenProjectDoesNotExist() {
        UUID ownerId = UUID.randomUUID();
        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(UUID.randomUUID(), ownerId));
    }

    @Test
    void throwsNotFoundWhenProjectBelongsToAnotherOwner() {
        UUID owner1 = UUID.randomUUID();
        UUID owner2 = UUID.randomUUID();
        Project saved = repository.save(Project.create("Private Project", owner1));

        assertThrows(ResourceNotFoundException.class, () -> useCase.execute(saved.getId(), owner2));
    }
}

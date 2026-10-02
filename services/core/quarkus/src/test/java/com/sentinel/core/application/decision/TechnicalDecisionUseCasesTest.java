package com.sentinel.core.application.decision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sentinel.core.adapter.out.memory.InMemoryProjectRepository;
import com.sentinel.core.adapter.out.memory.InMemoryTechnicalDecisionRepository;
import com.sentinel.core.domain.common.PageResult;
import com.sentinel.core.domain.common.exception.ResourceNotFoundException;
import com.sentinel.core.domain.decision.DecisionStatus;
import com.sentinel.core.domain.decision.TechnicalDecision;
import com.sentinel.core.domain.project.Project;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TechnicalDecisionUseCasesTest {

    private InMemoryProjectRepository projectRepository;
    private InMemoryTechnicalDecisionRepository decisionRepository;
    private CreateDecisionUseCase createUseCase;
    private ListDecisionsUseCase listUseCase;
    private GetDecisionUseCase getUseCase;

    @BeforeEach
    void setUp() {
        projectRepository = new InMemoryProjectRepository();
        decisionRepository = new InMemoryTechnicalDecisionRepository();
        createUseCase = new CreateDecisionUseCase(projectRepository, decisionRepository);
        listUseCase = new ListDecisionsUseCase(projectRepository, decisionRepository);
        getUseCase = new GetDecisionUseCase(projectRepository, decisionRepository);
    }

    @Test
    void createsAndRetrievesDecisionSuccessfully() {
        UUID ownerId = UUID.randomUUID();
        Project project = projectRepository.save(Project.create("Sentinel Core", ownerId));

        TechnicalDecision created = createUseCase.execute(
                project.getId(),
                ownerId,
                "Usar Quarkus",
                "Quarkus oferece alto desempenho e tempos rápidos de inicialização",
                DecisionStatus.ACCEPTED,
                "Lucas",
                LocalDate.now()
        );

        assertNotNull(created.getId());
        assertEquals(project.getId(), created.getProjectId());
        assertEquals("Usar Quarkus", created.getTitle());

        TechnicalDecision fetched = getUseCase.execute(project.getId(), created.getId(), ownerId);
        assertNotNull(fetched);
        assertEquals(created.getId(), fetched.getId());
    }

    @Test
    void throwsNotFoundWhenCreatingDecisionForNonExistentProject() {
        UUID ownerId = UUID.randomUUID();
        UUID nonExistentProject = UUID.randomUUID();

        assertThrows(ResourceNotFoundException.class, () -> createUseCase.execute(
                nonExistentProject,
                ownerId,
                "Decisão inválida",
                "Descrição",
                DecisionStatus.PROPOSED,
                "Lucas",
                LocalDate.now()
        ));
    }

    @Test
    void listsDecisionsWithStatusAndTitleFilters() {
        UUID ownerId = UUID.randomUUID();
        Project project = projectRepository.save(Project.create("Sentinel Core", ownerId));

        createUseCase.execute(
                project.getId(),
                ownerId,
                "PostgreSQL para persistência",
                "Robusto e relacional",
                DecisionStatus.ACCEPTED,
                "Lucas",
                LocalDate.now()
        );

        createUseCase.execute(
                project.getId(),
                ownerId,
                "MongoDB para logs",
                "Não relacional",
                DecisionStatus.REJECTED,
                "Lucas",
                LocalDate.now()
        );

        PageResult<TechnicalDecision> all = listUseCase.execute(project.getId(), ownerId, null, null, 0, 10);
        assertEquals(2, all.items().size());

        PageResult<TechnicalDecision> acceptedOnly = listUseCase.execute(project.getId(), ownerId, DecisionStatus.ACCEPTED, null, 0, 10);
        assertEquals(1, acceptedOnly.items().size());
        assertEquals("PostgreSQL para persistência", acceptedOnly.items().get(0).getTitle());

        PageResult<TechnicalDecision> filteredByTitle = listUseCase.execute(project.getId(), ownerId, null, "mongo", 0, 10);
        assertEquals(1, filteredByTitle.items().size());
        assertEquals(DecisionStatus.REJECTED, filteredByTitle.items().get(0).getStatus());
    }
}

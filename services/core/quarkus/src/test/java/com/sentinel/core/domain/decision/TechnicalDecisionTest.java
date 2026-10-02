package com.sentinel.core.domain.decision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TechnicalDecisionTest {

    @Test
    void createsDecisionWithDefaultStatusAndCurrentDate() {
        UUID projectId = UUID.randomUUID();
        TechnicalDecision decision = TechnicalDecision.create(
                projectId,
                "Adotar PostgreSQL",
                "Robustez e suporte relacional",
                null,
                "Lucas",
                null
        );

        assertNotNull(decision.getId());
        assertEquals(projectId, decision.getProjectId());
        assertEquals("Adotar PostgreSQL", decision.getTitle());
        assertEquals(DecisionStatus.PROPOSED, decision.getStatus());
        assertEquals("Lucas", decision.getAuthor());
        assertEquals(LocalDate.now(), decision.getDecisionDate());
        assertNotNull(decision.getCreatedAt());
        assertNotNull(decision.getUpdatedAt());
    }

    @Test
    void parsesDecisionStatusCaseInsensitive() {
        assertEquals(DecisionStatus.ACCEPTED, DecisionStatus.fromString("accepted"));
        assertEquals(DecisionStatus.ACCEPTED, DecisionStatus.fromString(" ACCEPTED "));
        assertEquals(DecisionStatus.DEPRECATED, DecisionStatus.fromString("deprecated"));
        assertNull(DecisionStatus.fromString(null));
        assertNull(DecisionStatus.fromString("   "));
    }

    @Test
    void throwsExceptionOnInvalidDecisionStatus() {
        assertThrows(IllegalArgumentException.class, () -> DecisionStatus.fromString("INVALID_STATUS"));
    }
}

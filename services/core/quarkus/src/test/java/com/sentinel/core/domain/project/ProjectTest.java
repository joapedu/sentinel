package com.sentinel.core.domain.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProjectTest {

    @Test
    void createsProjectWithDefaultValues() {
        UUID ownerId = UUID.randomUUID();
        Project project = Project.create("Sentinel Core", ownerId);

        assertNotNull(project.getId());
        assertEquals("Sentinel Core", project.getName());
        assertEquals(ownerId, project.getOwnerId());
        assertNotNull(project.getCreatedAt());
    }

    @Test
    void updatesProjectName() {
        UUID ownerId = UUID.randomUUID();
        Project project = Project.create("Old Name", ownerId);
        project.setName("New Name");

        assertEquals("New Name", project.getName());
    }
}

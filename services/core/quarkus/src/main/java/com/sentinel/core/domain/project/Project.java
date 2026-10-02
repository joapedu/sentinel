package com.sentinel.core.domain.project;

import java.time.Instant;
import java.util.UUID;

public class Project {
    private final UUID id;
    private String name;
    private final UUID ownerId;
    private final Instant createdAt;

    public Project(UUID id, String name, UUID ownerId, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    public static Project create(String name, UUID ownerId) {
        return new Project(UUID.randomUUID(), name, ownerId, Instant.now());
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }
}

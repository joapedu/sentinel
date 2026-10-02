package com.sentinel.core.domain.decision;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TechnicalDecision {
    private final UUID id;
    private final UUID projectId;
    private String title;
    private String description;
    private DecisionStatus status;
    private String author;
    private LocalDate decisionDate;
    private final Instant createdAt;
    private Instant updatedAt;

    public TechnicalDecision(
            UUID id,
            UUID projectId,
            String title,
            String description,
            DecisionStatus status,
            String author,
            LocalDate decisionDate,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.author = author;
        this.decisionDate = decisionDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static TechnicalDecision create(
            UUID projectId,
            String title,
            String description,
            DecisionStatus status,
            String author,
            LocalDate decisionDate
    ) {
        Instant now = Instant.now();
        return new TechnicalDecision(
                UUID.randomUUID(),
                projectId,
                title,
                description,
                status != null ? status : DecisionStatus.PROPOSED,
                author,
                decisionDate != null ? decisionDate : LocalDate.now(),
                now,
                now
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public DecisionStatus getStatus() {
        return status;
    }

    public String getAuthor() {
        return author;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(DecisionStatus status) {
        this.status = status;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDecisionDate(LocalDate decisionDate) {
        this.decisionDate = decisionDate;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

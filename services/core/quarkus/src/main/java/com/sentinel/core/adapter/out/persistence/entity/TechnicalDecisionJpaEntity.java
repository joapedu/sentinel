package com.sentinel.core.adapter.out.persistence.entity;

import com.sentinel.core.domain.decision.DecisionStatus;
import com.sentinel.core.domain.decision.TechnicalDecision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "technical_decisions")
public class TechnicalDecisionJpaEntity {
    @Id
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    public ProjectJpaEntity project;

    @Column(nullable = false, length = 200)
    public String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String description;

    @Column(nullable = false, length = 30)
    public String status;

    @Column(nullable = false, length = 120)
    public String author;

    @Column(name = "decision_date", nullable = false)
    public LocalDate decisionDate;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    public TechnicalDecisionJpaEntity() {
    }

    public TechnicalDecisionJpaEntity(
            UUID id,
            ProjectJpaEntity project,
            String title,
            String description,
            String status,
            String author,
            LocalDate decisionDate,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.project = project;
        this.title = title;
        this.description = description;
        this.status = status;
        this.author = author;
        this.decisionDate = decisionDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public TechnicalDecision toDomain() {
        return new TechnicalDecision(
                this.id,
                this.project.id,
                this.title,
                this.description,
                DecisionStatus.valueOf(this.status),
                this.author,
                this.decisionDate,
                this.createdAt,
                this.updatedAt
        );
    }

    public static TechnicalDecisionJpaEntity fromDomain(TechnicalDecision domain, ProjectJpaEntity project) {
        return new TechnicalDecisionJpaEntity(
                domain.getId(),
                project,
                domain.getTitle(),
                domain.getDescription(),
                domain.getStatus().name(),
                domain.getAuthor(),
                domain.getDecisionDate(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}

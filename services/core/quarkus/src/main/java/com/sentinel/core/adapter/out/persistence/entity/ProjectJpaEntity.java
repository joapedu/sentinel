package com.sentinel.core.adapter.out.persistence.entity;

import com.sentinel.core.auth.User;
import com.sentinel.core.domain.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
public class ProjectJpaEntity {
    @Id
    public UUID id;

    @Column(nullable = false, length = 120)
    public String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    public User owner;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    public ProjectJpaEntity() {
    }

    public ProjectJpaEntity(UUID id, String name, User owner, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.createdAt = createdAt;
    }

    public Project toDomain() {
        return new Project(this.id, this.name, this.owner.id, this.createdAt);
    }

    public static ProjectJpaEntity fromDomain(Project domain, User owner) {
        return new ProjectJpaEntity(domain.getId(), domain.getName(), owner, domain.getCreatedAt());
    }
}

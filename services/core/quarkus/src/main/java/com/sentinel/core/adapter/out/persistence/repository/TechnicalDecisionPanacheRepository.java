package com.sentinel.core.adapter.out.persistence.repository;

import com.sentinel.core.adapter.out.persistence.entity.TechnicalDecisionJpaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class TechnicalDecisionPanacheRepository implements PanacheRepositoryBase<TechnicalDecisionJpaEntity, UUID> {
}

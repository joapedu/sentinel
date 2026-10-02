package com.sentinel.core.domain.decision;

public enum DecisionStatus {
    PROPOSED,
    ACCEPTED,
    DEPRECATED,
    REJECTED;

    public static DecisionStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return DecisionStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + value + ". Valores aceitos: PROPOSED, ACCEPTED, DEPRECATED, REJECTED");
        }
    }
}

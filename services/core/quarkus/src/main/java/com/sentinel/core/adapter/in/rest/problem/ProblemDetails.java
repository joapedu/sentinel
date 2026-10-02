package com.sentinel.core.adapter.in.rest.problem;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemDetails(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        List<InvalidParam> invalidParams
) {
    public static final String MEDIA_TYPE = "application/problem+json";

    public static ProblemDetails of(String type, String title, int status, String detail) {
        return new ProblemDetails(type, title, status, detail, null, null);
    }

    public static ProblemDetails of(String type, String title, int status, String detail, String instance) {
        return new ProblemDetails(type, title, status, detail, instance, null);
    }

    public static ProblemDetails withViolations(String type, String title, int status, String detail, List<InvalidParam> invalidParams) {
        return new ProblemDetails(type, title, status, detail, null, invalidParams);
    }

    public record InvalidParam(String name, String reason) {
    }
}

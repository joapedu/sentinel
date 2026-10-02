package com.sentinel.core.adapter.in.rest.problem;

import jakarta.annotation.Priority;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;

@Provider
@Priority(Priorities.USER)
@Produces(ProblemDetails.MEDIA_TYPE)
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException cve) {
        List<ProblemDetails.InvalidParam> invalidParams = new ArrayList<>();
        for (ConstraintViolation<?> violation : cve.getConstraintViolations()) {
            String field = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "param";
            int lastDot = field.lastIndexOf('.');
            if (lastDot >= 0 && lastDot < field.length() - 1) {
                field = field.substring(lastDot + 1);
            }
            invalidParams.add(new ProblemDetails.InvalidParam(field, violation.getMessage()));
        }

        ProblemDetails problem = ProblemDetails.withViolations(
                "urn:sentinel:error:bad-request",
                "Bad Request",
                Response.Status.BAD_REQUEST.getStatusCode(),
                "Parâmetros de requisição inválidos",
                invalidParams
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .type(ProblemDetails.MEDIA_TYPE)
                .header("Content-Type", ProblemDetails.MEDIA_TYPE)
                .entity(problem)
                .build();
    }
}

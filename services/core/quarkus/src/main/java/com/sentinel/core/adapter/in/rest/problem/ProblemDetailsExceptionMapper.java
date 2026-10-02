package com.sentinel.core.adapter.in.rest.problem;

import com.sentinel.core.auth.EmailAlreadyRegisteredException;
import com.sentinel.core.auth.InvalidCredentialsException;
import com.sentinel.core.domain.common.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;

@Provider
@Produces(ProblemDetails.MEDIA_TYPE)
public class ProblemDetailsExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof ConstraintViolationException cve) {
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
            return buildResponse(Response.Status.BAD_REQUEST, problem);
        }

        if (exception instanceof ResourceNotFoundException rnfe) {
            ProblemDetails problem = ProblemDetails.of(
                    "urn:sentinel:error:not-found",
                    "Not Found",
                    Response.Status.NOT_FOUND.getStatusCode(),
                    rnfe.getMessage()
            );
            return buildResponse(Response.Status.NOT_FOUND, problem);
        }

        if (exception instanceof EmailAlreadyRegisteredException) {
            ProblemDetails problem = ProblemDetails.of(
                    "urn:sentinel:error:conflict",
                    "Conflict",
                    Response.Status.CONFLICT.getStatusCode(),
                    "Email já cadastrado"
            );
            return buildResponse(Response.Status.CONFLICT, problem);
        }

        if (exception instanceof InvalidCredentialsException) {
            ProblemDetails problem = ProblemDetails.of(
                    "urn:sentinel:error:unauthorized",
                    "Unauthorized",
                    Response.Status.UNAUTHORIZED.getStatusCode(),
                    "Credenciais inválidas"
            );
            return buildResponse(Response.Status.UNAUTHORIZED, problem);
        }

        if (exception instanceof IllegalArgumentException iae) {
            ProblemDetails problem = ProblemDetails.of(
                    "urn:sentinel:error:bad-request",
                    "Bad Request",
                    Response.Status.BAD_REQUEST.getStatusCode(),
                    iae.getMessage()
            );
            return buildResponse(Response.Status.BAD_REQUEST, problem);
        }

        if (exception instanceof WebApplicationException wae) {
            int status = wae.getResponse().getStatus();
            ProblemDetails problem = ProblemDetails.of(
                    "urn:sentinel:error:http-error",
                    wae.getMessage() != null ? wae.getMessage() : "HTTP Error",
                    status,
                    wae.getMessage()
            );
            return buildResponse(Response.Status.fromStatusCode(status), problem);
        }

        ProblemDetails problem = ProblemDetails.of(
                "urn:sentinel:error:internal-error",
                "Internal Server Error",
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Ocorreu um erro interno no servidor"
        );
        return buildResponse(Response.Status.INTERNAL_SERVER_ERROR, problem);
    }

    private Response buildResponse(Response.Status status, ProblemDetails problem) {
        return Response.status(status)
                .type(ProblemDetails.MEDIA_TYPE)
                .header("Content-Type", ProblemDetails.MEDIA_TYPE)
                .entity(problem)
                .build();
    }
}

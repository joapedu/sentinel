package com.sentinel.core.api;

import com.sentinel.core.auth.EmailAlreadyRegisteredException;
import com.sentinel.core.auth.InvalidCredentialsException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<RuntimeException> {
    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof EmailAlreadyRegisteredException) {
            return error(Response.Status.CONFLICT, "email_already_registered");
        }
        if (exception instanceof InvalidCredentialsException) {
            return error(Response.Status.UNAUTHORIZED, "invalid_credentials");
        }
        return error(Response.Status.INTERNAL_SERVER_ERROR, "internal_error");
    }

    private Response error(Response.Status status, String code) {
        return Response.status(status).type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(code))
                .build();
    }

    public record ErrorResponse(String code) {
    }
}
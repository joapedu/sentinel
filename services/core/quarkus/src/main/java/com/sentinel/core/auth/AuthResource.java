package com.sentinel.core.auth;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/v1/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject
    AuthService authService;

    @Inject
    JsonWebToken token;

    @POST
    @Path("/register")
    public Response register(@Valid AuthDtos.Credentials credentials) {
        return Response.status(Response.Status.CREATED).entity(authService.register(credentials)).build();
    }

    @POST
    @Path("/login")
    public AuthDtos.TokenResponse login(@Valid AuthDtos.Credentials credentials) {
        return authService.login(credentials);
    }

    @POST
    @Path("/refresh")
    public AuthDtos.TokenResponse refresh(@Valid AuthDtos.RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @POST
    @Path("/logout")
    public Response logout(@Valid AuthDtos.RefreshRequest request) {
        authService.logout(request.refreshToken());
        return Response.noContent().build();
    }

    @GET
    @Path("/me")
    @Authenticated
    public AuthDtos.MeResponse me() {
        User user = User.findById(java.util.UUID.fromString(token.getSubject()));
        if (user == null || !user.active) {
            throw new InvalidCredentialsException();
        }
        return AuthDtos.MeResponse.from(user);
    }
}
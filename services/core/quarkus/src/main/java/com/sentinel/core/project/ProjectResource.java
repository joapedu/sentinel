package com.sentinel.core.project;

import com.sentinel.core.auth.InvalidCredentialsException;
import com.sentinel.core.auth.User;
import com.sentinel.core.auth.InvalidCredentialsException;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/v1/projects")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectResource {
    @Inject
    JsonWebToken token;

    @POST
    @Transactional
    public Response create(@Valid ProjectDtos.CreateProject request) {
        Project project = new Project();
        project.name = request.name().trim();
        project.owner = currentUser();
        project.persist();
        return Response.status(Response.Status.CREATED).entity(ProjectDtos.ProjectResponse.from(project)).build();
    }

    @GET
    public List<ProjectDtos.ProjectResponse> list() {
        return Project.<Project>list("owner.id", currentUser().id).stream()
                .map(ProjectDtos.ProjectResponse::from)
                .toList();
    }

    private User currentUser() {
        User user = User.findById(UUID.fromString(token.getSubject()));
        if (user == null || !user.active) {
            throw new InvalidCredentialsException();
        }
        return user;
    }
}
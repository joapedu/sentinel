package com.sentinel.core.adapter.in.rest.project;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;

import com.sentinel.core.adapter.out.persistence.entity.ProjectJpaEntity;
import com.sentinel.core.adapter.out.persistence.repository.PanacheProjectRepository;
import com.sentinel.core.auth.User;
import com.sentinel.core.domain.project.Project;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ProjectResourceTest {

    @Inject
    PanacheProjectRepository projectRepository;

    private String token;
    private UUID userId;
    private Project project;

    @BeforeEach
    @Transactional
    void setUp() {
        User user = User.find("email", "project-test-user@example.com").firstResult();
        if (user == null) {
            user = new User();
            user.email = "project-test-user@example.com";
            user.passwordHash = BcryptUtil.bcryptHash("strong-pass-1234");
            user.active = true;
            user.persist();
        }
        userId = user.id;

        token = Jwt.issuer("sentinel-core")
                .audience("sentinel-api")
                .subject(userId.toString())
                .upn(user.email)
                .sign();

        Project domainProject = Project.create("Sentinel Core Test", userId);
        project = projectRepository.save(domainProject);
    }

    @Test
    void listsProjectsWithPaginationAndAuthentication() {
        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/v1/projects")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("items.size()", greaterThanOrEqualTo(1))
                .body("page", equalTo(0))
                .body("size", equalTo(10));
    }

    @Test
    void getsProjectById() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/projects/" + project.getId())
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(project.getId().toString()))
                .body("name", equalTo(project.getName()));
    }

    @Test
    void returns404WhenProjectDoesNotExist() {
        given()
                .header("Authorization", "Bearer " + token)
                .accept("application/problem+json, application/json")
                .when()
                .get("/api/v1/projects/" + UUID.randomUUID())
                .then()
                .statusCode(404)
                .contentType(org.hamcrest.Matchers.startsWith("application/problem+json"))
                .body("title", equalTo("Not Found"))
                .body("status", equalTo(404));
    }

    @Test
    void returns401WhenNoTokenProvided() {
        given()
                .when()
                .get("/api/v1/projects")
                .then()
                .statusCode(401);
    }
}

package com.sentinel.core.adapter.in.rest.decision;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.sentinel.core.adapter.out.persistence.repository.PanacheProjectRepository;
import com.sentinel.core.auth.User;
import com.sentinel.core.domain.project.Project;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TechnicalDecisionResourceTest {

    @Inject
    PanacheProjectRepository projectRepository;

    private String token;
    private UUID userId;
    private Project project;

    @BeforeEach
    @Transactional
    void setUp() {
        User user = User.find("email", "decision-test-user@example.com").firstResult();
        if (user == null) {
            user = new User();
            user.email = "decision-test-user@example.com";
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

        Project domainProject = Project.create("Sentinel Decision Test Project", userId);
        project = projectRepository.save(domainProject);
    }

    @Test
    void createsTechnicalDecisionReturns201AndLocation() {
        Map<String, Object> body = Map.of(
                "title", "Utilizar PostgreSQL no módulo de pagamentos",
                "description", "Necessidade de transações ACID e suporte relacional maduro.",
                "status", "ACCEPTED",
                "author", "Lucas Mangabeira",
                "decisionDate", "2026-10-02"
        );

        String decisionId = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/v1/projects/" + project.getId() + "/decisions")
                .then()
                .statusCode(201)
                .header("Location", not(emptyOrNullString()))
                .body("id", notNullValue())
                .body("title", equalTo("Utilizar PostgreSQL no módulo de pagamentos"))
                .body("status", equalTo("ACCEPTED"))
                .extract().path("id");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/projects/" + project.getId() + "/decisions/" + decisionId)
                .then()
                .statusCode(200)
                .body("id", equalTo(decisionId))
                .body("title", equalTo("Utilizar PostgreSQL no módulo de pagamentos"));
    }

    @Test
    void listsDecisionsWithPaginationAndFilters() {
        Map<String, Object> body = Map.of(
                "title", "Adotar Docker Compose",
                "description", "Padronização do ambiente local de banco e dependências.",
                "status", "ACCEPTED",
                "author", "Lucas",
                "decisionDate", "2026-10-02"
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/v1/projects/" + project.getId() + "/decisions")
                .then()
                .statusCode(201);

        given()
                .header("Authorization", "Bearer " + token)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .queryParam("status", "ACCEPTED")
                .queryParam("title", "docker")
                .when()
                .get("/api/v1/projects/" + project.getId() + "/decisions")
                .then()
                .statusCode(200)
                .body("items.size()", greaterThanOrEqualTo(1))
                .body("page", equalTo(0));
    }

    @Test
    void returns400ProblemDetailsWhenPayloadIsInvalid() {
        Map<String, Object> invalidBody = Map.of(
                "title", "",
                "description", "",
                "author", "",
                "decisionDate", "2026-10-02"
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept("application/problem+json, application/json")
                .body(invalidBody)
                .when()
                .post("/api/v1/projects/" + project.getId() + "/decisions")
                .then()
                .statusCode(400)
                .contentType(org.hamcrest.Matchers.startsWith("application/problem+json"))
                .body("type", equalTo("urn:sentinel:error:bad-request"))
                .body("title", equalTo("Bad Request"))
                .body("status", equalTo(400))
                .body("invalidParams", notNullValue());
    }

    @Test
    void returns404WhenProjectDoesNotExist() {
        Map<String, Object> body = Map.of(
                "title", "Decisão em projeto inexistente",
                "description", "Teste",
                "status", "PROPOSED",
                "author", "Lucas",
                "decisionDate", "2026-10-02"
        );

        given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .accept("application/problem+json, application/json")
                .body(body)
                .when()
                .post("/api/v1/projects/" + UUID.randomUUID() + "/decisions")
                .then()
                .statusCode(404)
                .contentType(org.hamcrest.Matchers.startsWith("application/problem+json"))
                .body("status", equalTo(404));
    }
}

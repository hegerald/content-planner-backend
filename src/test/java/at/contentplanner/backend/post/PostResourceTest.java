package at.contentplanner.backend.post;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Integrationstests über die echte REST-Schnittstelle (Quarkus + In-Memory-H2).
 */
@QuarkusTest
class PostResourceTest {

    private static final String FUTURE = Instant.now().plus(Duration.ofDays(30)).toString();

    @Test
    void fullLifecycle() {
        int id = createPost("Lifecycle", "LINKEDIN", "Hallo Welt", FUTURE);

        given().get("/api/posts/{id}", id).then()
                .statusCode(200)
                .body("status", equalTo("DRAFT"))
                .body("editable", equalTo(true))
                .body("allowedTransitions", contains("SCHEDULED"));

        changeStatus(id, "SCHEDULED").then().statusCode(200).body("status", equalTo("SCHEDULED"));
        changeStatus(id, "PUBLISHED").then().statusCode(200)
                .body("status", equalTo("PUBLISHED"))
                .body("publishedAt", notNullValue())
                .body("allowedTransitions", hasSize(0));

        given().delete("/api/posts/{id}", id).then()
                .statusCode(422)
                .body("code", equalTo("POST_NOT_DELETABLE"));
    }

    @Test
    void textTooLongForChannelIsRejected() {
        given().contentType(ContentType.JSON)
                .body(Map.of("title", "Zu lang", "text", "x".repeat(281), "channel", "X"))
                .post("/api/posts")
                .then()
                .statusCode(422)
                .body("code", equalTo("TEXT_TOO_LONG"));
    }

    @Test
    void missingTitleIsValidationError() {
        given().contentType(ContentType.JSON)
                .body(Map.of("text", "ohne Titel", "channel", "X"))
                .post("/api/posts")
                .then()
                .statusCode(400)
                .body("code", equalTo("VALIDATION_FAILED"));
    }

    @Test
    void unknownPostIs404() {
        given().get("/api/posts/{id}", 999_999).then()
                .statusCode(404)
                .body("code", equalTo("NOT_FOUND"));
    }

    @Test
    void updateAndDeleteDraft() {
        int id = createPost("Entwurf", "INSTAGRAM", "Version 1", null);

        given().contentType(ContentType.JSON)
                .body(Map.of("title", "Entwurf", "text", "Version 2", "channel", "INSTAGRAM"))
                .put("/api/posts/{id}", id)
                .then()
                .statusCode(200)
                .body("text", equalTo("Version 2"));

        given().delete("/api/posts/{id}", id).then().statusCode(204);
        given().get("/api/posts/{id}", id).then().statusCode(404);
    }

    @Test
    void channelsListContainsLimits() {
        given().get("/api/channels").then()
                .statusCode(200)
                .body("find { it.channel == 'X' }.maxLength", equalTo(280));
    }

    private static int createPost(String title, String channel, String text, String scheduledAt) {
        var body = new java.util.HashMap<String, Object>(Map.of("title", title, "text", text, "channel", channel));
        if (scheduledAt != null) {
            body.put("scheduledAt", scheduledAt);
        }
        return given().contentType(ContentType.JSON)
                .body(body)
                .post("/api/posts")
                .then()
                .statusCode(201)
                .body("status", equalTo("DRAFT"))
                .extract().path("id");
    }

    private static io.restassured.response.Response changeStatus(int id, String target) {
        return given().contentType(ContentType.JSON)
                .body(Map.of("targetStatus", target))
                .post("/api/posts/{id}/status", id);
    }
}

package io.parser.useraggregator.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.parser.useraggregator.dto.UserResponse;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class UserControllerIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("users_db")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("testcontainers/postgres-init.sql");

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("users_db")
            .withUsername("mysql")
            .withPassword("mysql")
            .withInitScript("testcontainers/mysql-init.sql");

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("data-sources[0].name", () -> "postgres-users");
        registry.add("data-sources[0].strategy", () -> "postgres");
        registry.add("data-sources[0].url", POSTGRES::getJdbcUrl);
        registry.add("data-sources[0].table", () -> "public.app_users");
        registry.add("data-sources[0].user", POSTGRES::getUsername);
        registry.add("data-sources[0].password", POSTGRES::getPassword);
        registry.add("data-sources[0].mapping.id", () -> "external_id");
        registry.add("data-sources[0].mapping.username", () -> "login");
        registry.add("data-sources[0].mapping.name", () -> "first_name");
        registry.add("data-sources[0].mapping.surname", () -> "last_name");

        registry.add("data-sources[1].name", () -> "mysql-users");
        registry.add("data-sources[1].strategy", () -> "mysql");
        registry.add("data-sources[1].url", MYSQL::getJdbcUrl);
        registry.add("data-sources[1].table", () -> "legacy_users");
        registry.add("data-sources[1].user", MYSQL::getUsername);
        registry.add("data-sources[1].password", MYSQL::getPassword);
        registry.add("data-sources[1].mapping.id", () -> "user_identifier");
        registry.add("data-sources[1].mapping.username", () -> "user_name");
        registry.add("data-sources[1].mapping.name", () -> "given_name");
        registry.add("data-sources[1].mapping.surname", () -> "family_name");

        registry.add("data-sources[2].name", () -> "broken-source");
        registry.add("data-sources[2].strategy", () -> "postgres");
        registry.add("data-sources[2].url", () -> "jdbc:postgresql://localhost:1/unreachable");
        registry.add("data-sources[2].table", () -> "public.app_users");
        registry.add("data-sources[2].user", () -> "postgres");
        registry.add("data-sources[2].password", () -> "postgres");
        registry.add("data-sources[2].mapping.id", () -> "external_id");
        registry.add("data-sources[2].mapping.username", () -> "login");
        registry.add("data-sources[2].mapping.name", () -> "first_name");
        registry.add("data-sources[2].mapping.surname", () -> "last_name");
    }

    @Test
    void shouldAggregateUsersAndIgnoreFailedSource() throws Exception {
        HttpResponse<String> response = get("/api/v1/users");
        UserResponse[] users = jsonMapper.readValue(response.body(), UserResponse[].class);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(users).containsExactly(
                new UserResponse("1", "jdoe", "John", "Anderson"),
                new UserResponse("2", "asmith", "Alice", "Smith"),
                new UserResponse("3", "bwayne", "Bruce", "Anderson"),
                new UserResponse("4", "td.pn", "Todd", "Peterson"));
    }

    @Test
    void shouldFilterUsersAcrossAllConfiguredSources() throws Exception {
        HttpResponse<String> response = get("/api/v1/users?surname=Anderson");
        UserResponse[] users = jsonMapper.readValue(response.body(), UserResponse[].class);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(users).containsExactly(
                new UserResponse("1", "jdoe", "John", "Anderson"),
                new UserResponse("3", "bwayne", "Bruce", "Anderson"));
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:%d%s".formatted(port, path)))
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

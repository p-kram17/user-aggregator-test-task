package io.parser.useraggregator.service;

import io.parser.useraggregator.config.AppDataSourceProperties;
import io.parser.useraggregator.config.DataSourceRegistry;
import io.parser.useraggregator.config.SourceContext;
import io.parser.useraggregator.dto.UserFilterRequest;
import io.parser.useraggregator.dto.UserResponse;
import io.parser.useraggregator.exception.SourceAccessException;
import io.parser.useraggregator.repository.AggregatedUserRepository;
import io.parser.useraggregator.specification.UserFilterSpecification;
import io.parser.useraggregator.strategy.MySqlDatabaseQueryStrategy;
import io.parser.useraggregator.strategy.PostgresDatabaseQueryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class UserAggregationServiceTest {

    @Mock
    private DataSourceRegistry dataSourceRegistry;

    @Mock
    private AggregatedUserRepository aggregatedUserRepository;

    private Executor testExecutor;

    private UserAggregationService service;

    @Captor
    private ArgumentCaptor<UserFilterSpecification> specCaptor;

    @BeforeEach
    void setUp() {
        testExecutor = Executors.newSingleThreadExecutor();
        service = new UserAggregationService(dataSourceRegistry, aggregatedUserRepository, testExecutor);
    }

    @Test
    @DisplayName("Should deduplicate users by ID and skip failed sources")
    void shouldDeduplicateUsersByIdAndSkipFailedSources(CapturedOutput output) throws Exception {
        var postgresSource = createSource("postgres-users", "jdbc:postgresql://localhost/test", "public.users");
        var mysqlSource    = createSource("mysql-users",    "jdbc:mysql://localhost/test",    "users");
        var failedSource   = createSource("failed-source",  "jdbc:postgresql://localhost/missing", "users");

        when(dataSourceRegistry.getSources()).thenReturn(List.of(postgresSource, mysqlSource, failedSource));

        when(aggregatedUserRepository.findUsers(eq(postgresSource), any()))
                .thenReturn(List.of(
                        user("1", "jdoe",   "John",  "Doe"),
                        user("2", "asmith", "Alice", "Smith"),
                        user("4", "td.pn",  "Todd",  "Peterson")
                ));

        when(aggregatedUserRepository.findUsers(eq(mysqlSource), any()))
                .thenReturn(List.of(
                        user("2", "duplicate", "Alice", "Smith"),  // дубль
                        user("3", "bwayne",    "Bruce", "Wayne")
                ));

        when(aggregatedUserRepository.findUsers(eq(failedSource), any()))
                .thenThrow(new SourceAccessException("failed-source", new RuntimeException("Connection refused")));

        UserFilterRequest filter = new UserFilterRequest(null, null, null);

        CompletableFuture<List<UserResponse>> future = service.getUsersAsync(filter);
        List<UserResponse> result = future.get();

        assertThat(result)
                .hasSize(4)
                .extracting(UserResponse::id)
                .containsExactlyInAnyOrder("1", "2", "3", "4");

        assertThat(result)
                .filteredOn(u -> u.id().equals("2"))
                .singleElement()
                .satisfies(u -> {
                    assertThat(u.username()).isEqualTo("asmith");
                    assertThat(u.name()).isEqualTo("Alice");
                    assertThat(u.surname()).isEqualTo("Smith");
                });

        assertThat(output)
                .contains("Duplicate user id '2' detected. Skipping new value:")
                .contains("UserResponse[id=2, username=duplicate, name=Alice, surname=Smith]");

        verify(aggregatedUserRepository, times(3)).findUsers(any(), specCaptor.capture());
        assertThat(specCaptor.getAllValues())
                .allSatisfy(spec -> assertThat(spec).isNotNull());
    }

    @Test
    @DisplayName("Should return empty result even when sources are empty")
    void shouldReturnEmptyListWhenNoSourcesConfigured() throws Exception {
        when(dataSourceRegistry.getSources()).thenReturn(List.of());

        CompletableFuture<List<UserResponse>> future = service.getUsersAsync(new UserFilterRequest(null, null, null));

        assertThat(future.get()).isEmpty();
    }

    @Test
    @DisplayName("Should return empty result even when all sources fail")
    void shouldReturnEmptyResultWhenAllSourcesFail() throws Exception {
        SourceContext source = createSource("bad", "jdbc:...", "table");
        when(dataSourceRegistry.getSources()).thenReturn(List.of(source));
        when(aggregatedUserRepository.findUsers(any(), any()))
                .thenThrow(new SourceAccessException("bad", new RuntimeException()));

        CompletableFuture<List<UserResponse>> future = service.getUsersAsync(new UserFilterRequest(null, null, null));

        assertThat(future.get()).isEmpty();
    }

    private static UserResponse user(String id, String username, String name, String surname) {
        return new UserResponse(id, username, name, surname);
    }

    private SourceContext createSource(String name, String url, String table) {
        var props = new AppDataSourceProperties(
                name,
                url.contains("mysql") ? io.parser.useraggregator.strategy.DatabaseStrategy.MYSQL
                        : io.parser.useraggregator.strategy.DatabaseStrategy.POSTGRES,
                url,
                table,
                "user",
                "pass",
                new AppDataSourceProperties.Mapping("id", "username", "name", "surname")
        );

        var strategy = url.contains("mysql")
                ? new MySqlDatabaseQueryStrategy()
                : new PostgresDatabaseQueryStrategy();

        return new SourceContext(props, mock(), strategy);
    }
}

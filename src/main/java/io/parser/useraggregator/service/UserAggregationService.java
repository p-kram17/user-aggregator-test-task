package io.parser.useraggregator.service;

import io.parser.useraggregator.config.SourceContext;
import io.parser.useraggregator.config.DataSourceRegistry;
import io.parser.useraggregator.dto.UserFilterRequest;
import io.parser.useraggregator.dto.UserResponse;
import io.parser.useraggregator.exception.SourceAccessException;
import io.parser.useraggregator.repository.AggregatedUserRepository;
import io.parser.useraggregator.specification.UserFilterSpecification;
import io.parser.useraggregator.specification.UserSpecifications;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletionException;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAggregationService {

    private static final Duration FETCH_TIMEOUT = Duration.ofSeconds(10);

    private final DataSourceRegistry dataSourceRegistry;
    private final AggregatedUserRepository aggregatedUserRepository;
    private final Executor aggregationExecutor;

    public List<UserResponse> getUsers(UserFilterRequest request) {
        try {
            return getUsersAsync(request)
                    .orTimeout(30, TimeUnit.SECONDS)
                    .join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            throw new RuntimeException("Aggregation failed", cause);
        }
    }

    @Async("aggregationExecutor")
    public CompletableFuture<List<UserResponse>> getUsersAsync(UserFilterRequest request) {
        UserFilterSpecification spec = UserSpecifications.fromRequest(request);
        List<SourceContext> sources = dataSourceRegistry.getSources();

        if (sources.isEmpty()) {
            return CompletableFuture.completedFuture(List.of());
        }

        List<CompletableFuture<List<UserResponse>>> futures = sources.stream()
                .map(source -> fetchUsersWithTimeout(source, spec))
                .toList();

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .thenApply(v -> collectUniqueUsers(futures))
                .exceptionally(ex -> {
                    log.error("Unexpected error during aggregation", ex);
                    return List.of();
                });
    }

    private CompletableFuture<List<UserResponse>> fetchUsersWithTimeout(
            SourceContext source,
            UserFilterSpecification spec) {

        return CompletableFuture.supplyAsync(
                        () -> fetchUsersSafely(source, spec),
                        aggregationExecutor)
                .orTimeout(FETCH_TIMEOUT.getSeconds(), TimeUnit.SECONDS)
                .exceptionally(ex -> {
                    log.warn("Source {} failed or timed out: {}", source.properties().name(), ex.getMessage());
                    return List.of();
                });
    }

    private List<UserResponse> collectUniqueUsers(List<CompletableFuture<List<UserResponse>>> futures) {
        Map<String, UserResponse> unique = new LinkedHashMap<>();

        futures.stream()
                .map(CompletableFuture::join)   // безпечно, бо allOf вже виконався
                .flatMap(List::stream)
                .forEach(user -> {
                    UserResponse existing = unique.putIfAbsent(user.id(), user);
                    if (existing != null) {
                        log.warn("Duplicate user id '{}' detected. Skipping new value: {}", user.id(), user);
                    }
                });

        return new ArrayList<>(unique.values());
    }

    private List<UserResponse> fetchUsersSafely(SourceContext source, UserFilterSpecification specification) {
        try {
            return aggregatedUserRepository.findUsers(source, specification);
        } catch (SourceAccessException exception) {
            log.error("Skipping failed data source '{}': {}", source.properties().name(), exception.getMessage());
            return List.of();
        }
    }
}

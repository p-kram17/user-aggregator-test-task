package io.parser.useraggregator.config;

import io.parser.useraggregator.strategy.DatabaseQueryStrategy;
import org.springframework.jdbc.core.JdbcTemplate;

public record SourceContext(
        AppDataSourceProperties properties,
        JdbcTemplate jdbcTemplate,
        DatabaseQueryStrategy queryStrategy
) {}

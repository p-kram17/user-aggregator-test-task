package io.parser.useraggregator.strategy;

import io.parser.useraggregator.config.AppDataSourceProperties;
import io.parser.useraggregator.specification.SqlFilterClause;

public interface DatabaseQueryStrategy {

    DatabaseStrategy type();

    String quoteIdentifier(String identifier);

    String buildSelectUsersQuery(AppDataSourceProperties properties, SqlFilterClause filterClause);
}

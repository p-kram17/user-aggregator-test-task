package io.parser.useraggregator.specification;

import io.parser.useraggregator.config.AppDataSourceProperties;
import io.parser.useraggregator.strategy.DatabaseQueryStrategy;

public interface UserFilterSpecification {

    SqlFilterClause toSql(AppDataSourceProperties.Mapping mapping, DatabaseQueryStrategy queryStrategy);
}

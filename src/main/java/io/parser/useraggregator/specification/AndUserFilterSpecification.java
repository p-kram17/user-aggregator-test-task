package io.parser.useraggregator.specification;

import io.parser.useraggregator.config.AppDataSourceProperties;
import io.parser.useraggregator.strategy.DatabaseQueryStrategy;
import java.util.ArrayList;
import java.util.List;

public class AndUserFilterSpecification implements UserFilterSpecification {

    private final List<UserFilterSpecification> specifications;

    public AndUserFilterSpecification(List<UserFilterSpecification> specifications) {
        this.specifications = List.copyOf(specifications);
    }

    @Override
    public SqlFilterClause toSql(
            AppDataSourceProperties.Mapping mapping,
            DatabaseQueryStrategy queryStrategy
    ) {
        List<String> clauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        for (UserFilterSpecification specification : specifications) {
            SqlFilterClause filterClause = specification.toSql(mapping, queryStrategy);
            if (!filterClause.isEmpty()) {
                clauses.add("(" + filterClause.sql() + ")");
                parameters.addAll(filterClause.parameters());
            }
        }

        if (clauses.isEmpty()) {
            return SqlFilterClause.empty();
        }

        return new SqlFilterClause(String.join(" AND ", clauses), List.copyOf(parameters));
    }
}

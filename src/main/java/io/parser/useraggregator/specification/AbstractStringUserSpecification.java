package io.parser.useraggregator.specification;

import io.parser.useraggregator.config.AppDataSourceProperties;
import io.parser.useraggregator.strategy.DatabaseQueryStrategy;
import java.util.List;
import java.util.Locale;
import org.springframework.util.StringUtils;

public abstract class AbstractStringUserSpecification implements UserFilterSpecification {

    private final String value;

    protected AbstractStringUserSpecification(String value) {
        this.value = value;
    }

    @Override
    public SqlFilterClause toSql(
            AppDataSourceProperties.Mapping mapping,
            DatabaseQueryStrategy queryStrategy
    ) {
        if (!StringUtils.hasText(value)) {
            return SqlFilterClause.empty();
        }

        String column = resolveColumn(mapping);
        return new SqlFilterClause(
                "LOWER(%s) = ?".formatted(queryStrategy.quoteIdentifier(column)),
                List.of(value.trim().toLowerCase(Locale.ROOT)));
    }

    protected abstract String resolveColumn(AppDataSourceProperties.Mapping mapping);
}

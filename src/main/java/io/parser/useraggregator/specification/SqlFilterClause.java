package io.parser.useraggregator.specification;

import java.util.List;

public record SqlFilterClause(String sql, List<Object> parameters) {

    private static final SqlFilterClause EMPTY = new SqlFilterClause("", List.of());

    public static SqlFilterClause empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return sql == null || sql.isBlank();
    }
}

package io.parser.useraggregator.strategy;

import io.parser.useraggregator.config.AppDataSourceProperties;
import io.parser.useraggregator.exception.InvalidConfigurationException;
import io.parser.useraggregator.specification.SqlFilterClause;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class AbstractDatabaseQueryStrategy implements DatabaseQueryStrategy {

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_$]*");

    private final String quoteCharacter;

    protected AbstractDatabaseQueryStrategy(String quoteCharacter) {
        this.quoteCharacter = quoteCharacter;
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return Arrays.stream(identifier.split("\\."))
                .map(this::quoteSingleIdentifier)
                .collect(Collectors.joining("."));
    }

    @Override
    public String buildSelectUsersQuery(AppDataSourceProperties properties, SqlFilterClause filterClause) {
        String idColumn = quoteIdentifier(properties.mapping().id());
        String usernameColumn = quoteIdentifier(properties.mapping().username());
        String nameColumn = quoteIdentifier(properties.mapping().name());
        String surnameColumn = quoteIdentifier(properties.mapping().surname());
        String table = quoteIdentifier(properties.table());

        StringBuilder sqlBuilder = new StringBuilder()
                .append("SELECT ")
                .append(idColumn).append(" AS id, ")
                .append(usernameColumn).append(" AS username, ")
                .append(nameColumn).append(" AS name, ")
                .append(surnameColumn).append(" AS surname ")
                .append("FROM ").append(table);

        if (!filterClause.isEmpty()) {
            sqlBuilder.append(" WHERE ").append(filterClause.sql());
        }

        sqlBuilder.append(" ORDER BY ").append(idColumn);

        return sqlBuilder.toString();
    }

    private String quoteSingleIdentifier(String identifierPart) {
        if (!IDENTIFIER_PATTERN.matcher(identifierPart).matches()) {
            throw new InvalidConfigurationException("Unsafe SQL identifier configured: " + identifierPart);
        }

        return quoteCharacter + identifierPart + quoteCharacter;
    }
}

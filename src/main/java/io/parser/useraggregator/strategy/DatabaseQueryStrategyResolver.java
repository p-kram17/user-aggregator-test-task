package io.parser.useraggregator.strategy;

import io.parser.useraggregator.exception.InvalidConfigurationException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DatabaseQueryStrategyResolver {

    private final Map<DatabaseStrategy, DatabaseQueryStrategy> strategies;

    public DatabaseQueryStrategyResolver(List<DatabaseQueryStrategy> strategies) {
        this.strategies = new EnumMap<>(DatabaseStrategy.class);
        for (DatabaseQueryStrategy strategy : strategies) {
            this.strategies.put(strategy.type(), strategy);
        }
    }

    public DatabaseQueryStrategy resolve(DatabaseStrategy databaseStrategy) {
        DatabaseQueryStrategy strategy = strategies.get(databaseStrategy);
        if (strategy == null) {
            throw new InvalidConfigurationException("Unsupported database strategy: " + databaseStrategy);
        }

        return strategy;
    }
}

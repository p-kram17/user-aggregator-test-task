package io.parser.useraggregator.strategy;

import org.springframework.stereotype.Component;

@Component
public class PostgresDatabaseQueryStrategy extends AbstractDatabaseQueryStrategy {

    public PostgresDatabaseQueryStrategy() {
        super("\"");
    }

    @Override
    public DatabaseStrategy type() {
        return DatabaseStrategy.POSTGRES;
    }
}

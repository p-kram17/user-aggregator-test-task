package io.parser.useraggregator.strategy;

import org.springframework.stereotype.Component;

@Component
public class MySqlDatabaseQueryStrategy extends AbstractDatabaseQueryStrategy {

    public MySqlDatabaseQueryStrategy() {
        super("`");
    }

    @Override
    public DatabaseStrategy type() {
        return DatabaseStrategy.MYSQL;
    }
}

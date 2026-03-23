package io.parser.useraggregator.specification;

import io.parser.useraggregator.config.AppDataSourceProperties;

public class UsernameSpecification extends AbstractStringUserSpecification {

    public UsernameSpecification(String value) {
        super(value);
    }

    @Override
    protected String resolveColumn(AppDataSourceProperties.Mapping mapping) {
        return mapping.username();
    }
}

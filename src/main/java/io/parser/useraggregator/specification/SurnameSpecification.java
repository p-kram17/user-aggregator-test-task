package io.parser.useraggregator.specification;

import io.parser.useraggregator.config.AppDataSourceProperties;

public class SurnameSpecification extends AbstractStringUserSpecification {

    public SurnameSpecification(String value) {
        super(value);
    }

    @Override
    protected String resolveColumn(AppDataSourceProperties.Mapping mapping) {
        return mapping.surname();
    }
}

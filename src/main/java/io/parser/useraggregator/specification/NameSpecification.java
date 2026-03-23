package io.parser.useraggregator.specification;

import io.parser.useraggregator.config.AppDataSourceProperties;

public class NameSpecification extends AbstractStringUserSpecification {

    public NameSpecification(String value) {
        super(value);
    }

    @Override
    protected String resolveColumn(AppDataSourceProperties.Mapping mapping) {
        return mapping.name();
    }
}

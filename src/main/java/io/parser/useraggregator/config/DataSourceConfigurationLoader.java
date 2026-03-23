package io.parser.useraggregator.config;

import io.parser.useraggregator.exception.InvalidConfigurationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSourceConfigurationLoader {

    private static final Bindable<List<AppDataSourceProperties>> DATA_SOURCES_BINDABLE =
            Bindable.listOf(AppDataSourceProperties.class);

    private final Environment environment;
    private final Validator validator;

    public List<AppDataSourceProperties> load() {
        List<AppDataSourceProperties> properties = Binder.get(environment)
                .bind("data-sources", DATA_SOURCES_BINDABLE)
                .orElse(List.of());

        validate(properties);
        ensureUniqueNames(properties);

        return List.copyOf(properties);
    }

    private void validate(List<AppDataSourceProperties> properties) {
        for (AppDataSourceProperties property : properties) {
            Set<ConstraintViolation<AppDataSourceProperties>> violations = validator.validate(property);
            if (!violations.isEmpty()) {
                String message = violations.stream()
                        .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                        .collect(Collectors.joining(", "));
                throw new InvalidConfigurationException(
                        "Invalid configuration for data source '%s': %s".formatted(property.name(), message));
            }
        }
    }

    private void ensureUniqueNames(List<AppDataSourceProperties> properties) {
        Set<String> names = new LinkedHashSet<>();
        for (AppDataSourceProperties property : properties) {
            if (!names.add(property.name())) {
                throw new InvalidConfigurationException(
                        "Duplicate data source name detected: " + property.name());
            }
        }
    }
}

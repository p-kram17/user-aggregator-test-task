package io.parser.useraggregator.config;

import io.parser.useraggregator.strategy.DatabaseStrategy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppDataSourceProperties(
        @NotBlank String name,
        @NotNull DatabaseStrategy strategy,
        @NotBlank String url,
        @NotBlank String table,
        @NotBlank String user,
        @NotBlank String password,
        @Valid @NotNull Mapping mapping
) {

    public record Mapping(
            @NotBlank String id,
            @NotBlank String username,
            @NotBlank String name,
            @NotBlank String surname
    ) {
    }
}

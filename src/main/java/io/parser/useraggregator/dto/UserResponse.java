package io.parser.useraggregator.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Unified user view returned by the API")
public record UserResponse(
        @Schema(example = "42")
        String id,
        @Schema(example = "jdoe")
        String username,
        @Schema(example = "John")
        String name,
        @Schema(example = "Doe")
        String surname
) {
}

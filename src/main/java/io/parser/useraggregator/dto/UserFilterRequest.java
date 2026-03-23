package io.parser.useraggregator.dto;

public record UserFilterRequest(
        String username,
        String name,
        String surname
) {
}

package io.parser.useraggregator.exception;

public class SourceAccessException extends RuntimeException {

    public SourceAccessException(String sourceName, Throwable cause) {
        super("Failed to fetch users from data source '%s'".formatted(sourceName), cause);
    }
}

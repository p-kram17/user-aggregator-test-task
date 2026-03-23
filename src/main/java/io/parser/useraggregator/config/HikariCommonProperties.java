package io.parser.useraggregator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app.hikari")
@Component
@Data
public class HikariCommonProperties {
    private int maximumPoolSize;
    private int minimumIdle;
    private long initializationFailTimeout;
    private long connectionTimeout;
    private long validationTimeout;
}
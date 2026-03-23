package io.parser.useraggregator.config;

import com.zaxxer.hikari.HikariDataSource;
import io.parser.useraggregator.strategy.DatabaseQueryStrategyResolver;
import jakarta.annotation.PreDestroy;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;

@Component
@Slf4j
@Getter
public class DataSourceRegistry {
    private final List<SourceContext> sources;
    private final HikariCommonProperties commonProperties;

    public DataSourceRegistry(
            DataSourceConfigurationLoader configurationLoader,
            DatabaseQueryStrategyResolver strategyResolver,
            HikariCommonProperties commonProperties
    ) {
        this.commonProperties = commonProperties;
        this.sources = configurationLoader.load().stream()
                .map(properties -> new SourceContext(
                        properties,
                        new JdbcTemplate(createDataSource(properties)),
                        strategyResolver.resolve(properties.strategy())))
                .toList();


        log.info("Registered {} configured data sources", sources.size());
    }

    @PreDestroy
    public void shutdown() {
        for (SourceContext source : sources) {
            DataSource dataSource = source.jdbcTemplate().getDataSource();
            if (dataSource instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception exception) {
                    log.warn("Failed to close data source '{}'", source.properties().name(), exception);
                }
            }
        }
    }

    private DataSource createDataSource(AppDataSourceProperties properties) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("source-" + properties.name());
        dataSource.setJdbcUrl(properties.url());
        dataSource.setUsername(properties.user());
        dataSource.setPassword(properties.password());
        dataSource.setMaximumPoolSize(commonProperties.getMaximumPoolSize());
        dataSource.setMinimumIdle(commonProperties.getMinimumIdle());
        dataSource.setInitializationFailTimeout(commonProperties.getInitializationFailTimeout());
        dataSource.setConnectionTimeout(commonProperties.getConnectionTimeout());
        dataSource.setValidationTimeout(commonProperties.getValidationTimeout());
        return dataSource;
    }
}

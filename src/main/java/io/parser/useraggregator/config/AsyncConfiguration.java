package io.parser.useraggregator.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsyncConfiguration {

    @Bean(destroyMethod = "shutdown")
    public Executor aggregationExecutor() {
        int poolSize = Math.max(4, Runtime.getRuntime().availableProcessors());
        return Executors.newFixedThreadPool(poolSize);
    }
}

package io.parser.useraggregator.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI userAggregatorOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Aggregator API")
                        .version("v1")
                        .description("Aggregates user records from multiple configured relational databases"));
    }
}

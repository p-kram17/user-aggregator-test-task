package io.parser.useraggregator;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.SpringApplication;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class UserAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserAggregatorApplication.class, args);
    }
}

package se.gymnasieval;

import graphql.analysis.MaxQueryDepthInstrumentation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Gemensam GraphQL-konfiguration för hela appen. */
@Configuration
class GraphQlConfig {

    /**
     * API:et är öppet, så djupt nästlade frågor nekas innan de körs.
     * Spring Boot plockar upp alla Instrumentation-beans automatiskt.
     */
    @Bean
    MaxQueryDepthInstrumentation maxQueryDepthInstrumentation() {
        return new MaxQueryDepthInstrumentation(10);
    }
}

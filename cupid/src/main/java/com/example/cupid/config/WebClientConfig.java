package com.example.cupid.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${cupid.api.base-url}")
    private String baseUrl;

    @Value("${cupid.api.api-key}")
    private String apiKey;

    @Bean
    public WebClient cupidWebClient(WebClient.Builder builder) {
        // increase buffer if large payloads
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        return builder
                .baseUrl(baseUrl)
                .exchangeStrategies(strategies)
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }
}

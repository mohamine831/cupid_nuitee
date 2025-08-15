package com.example.cupid.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class CupidApiClient {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public Mono<JsonNode> testConnection() {
        log.info("Testing API connection to base URL...");
        return webClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(response -> log.info("API connection test successful: {}", response))
                .doOnError(error -> log.error("API connection test failed", error));
    }

    public Mono<String> testBaseUrl() {
        log.info("Testing base URL connectivity...");
        return webClient.get()
                .uri("/")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(response -> log.info("Base URL test successful: {}", response))
                .doOnError(error -> log.error("Base URL test failed", error));
    }

    public Mono<JsonNode> fetchProperty(long hotelId) {
        log.info("Fetching property data for hotel ID: {}", hotelId);

        return webClient.get()
                .uri(uriBuilder -> {
                    String path = "/property/" + hotelId;
                    log.info("Constructing URI with path: {}", path);
                    return uriBuilder.path(path).build();
                })
                .headers(headers -> {
                    log.info("Request headers: {}", headers);
                })
                .retrieve()
                .onStatus(status -> status.isError(),
                        response -> {
                            log.error("HTTP error {} for hotel ID: {}", response.statusCode(), hotelId);
                            return response.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        log.error("Error response body: {}", body);
                                        return Mono.error(new RuntimeException("HTTP " + response.statusCode() + ": " + body));
                                    });
                        })
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .doOnSuccess(response -> log.info("Successfully fetched property data for hotel ID: {}. Response: {}", hotelId, response))
                .doOnError(error -> log.error("Failed to fetch property data for hotel ID: {}", hotelId, error));
    }

    public Mono<JsonNode> fetchTranslation(long hotelId, String lang) {
        log.debug("Fetching translation for hotel ID: {} and language: {}", hotelId, lang);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/property/{hotelId}/lang/{lang}").build(hotelId, lang))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                .doOnSuccess(response -> log.debug("Successfully fetched translation for hotel ID: {} and language: {}", hotelId, lang))
                .doOnError(error -> log.error("Failed to fetch translation for hotel ID: {} and language: {}", hotelId, lang, error));
    }

    public Mono<JsonNode> fetchReviews(long hotelId, int count) {
        log.debug("Fetching {} reviews for hotel ID: {}", count, hotelId);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/property/reviews/{hotelId}/{count}").build(hotelId, count))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                .doOnSuccess(response -> log.debug("Successfully fetched {} reviews for hotel ID: {}", count, hotelId))
                .doOnError(error -> log.error("Failed to fetch reviews for hotel ID: {}", hotelId, error));
    }
}

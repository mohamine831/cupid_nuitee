package com.example.cupid.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CupidApiClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private ResponseSpec responseSpec;

    @InjectMocks
    private CupidApiClient cupidApiClient;

    private ObjectMapper objectMapper;
    private ObjectNode testResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        testResponse = objectMapper.createObjectNode();
        testResponse.put("test", "value");
    }

    @Test
    void testConnection_Success() {
        // Given
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(testResponse));

        // When & Then
        Mono<JsonNode> result = cupidApiClient.testConnection();
        assertThat(result.block()).isEqualTo(testResponse);

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/");
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).bodyToMono(JsonNode.class);
    }

    @Test
    void testConnection_Error() {
        // Given
        RuntimeException error = new RuntimeException("Connection failed");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.error(error));

        // When & Then
        StepVerifier.create(cupidApiClient.testConnection())
                .expectError(RuntimeException.class)
                .verify();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/");
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).bodyToMono(JsonNode.class);
    }

    @Test
    void testBaseUrl_Success() {
        // Given
        String expectedResponse = "Success";
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse));

        // When & Then
        StepVerifier.create(cupidApiClient.testBaseUrl())
                .expectNext(expectedResponse)
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/");
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).bodyToMono(String.class);
    }

    @Test
    void testBaseUrl_Error() {
        // Given
        RuntimeException error = new RuntimeException("Base URL test failed");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/")).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(error));

        // When & Then
        StepVerifier.create(cupidApiClient.testBaseUrl())
                .expectError(RuntimeException.class)
                .verify();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/");
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).bodyToMono(String.class);
    }

    @Test
    void fetchProperty_Success() {
        // Given
        long hotelId = 1270324L;
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(testResponse));

        // When & Then
        StepVerifier.create(cupidApiClient.fetchProperty(hotelId))
                .expectNext(testResponse)
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersUriSpec).headers(any());
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).onStatus(any(), any());
        verify(responseSpec).bodyToMono(JsonNode.class);
    }

    @Test
    void fetchProperty_Error() {
        // Given
        long hotelId = 1270324L;
        RuntimeException error = new RuntimeException("Property fetch failed");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.error(error));

        // When & Then
        StepVerifier.create(cupidApiClient.fetchProperty(hotelId))
                .expectError(RuntimeException.class)
                .verify();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersUriSpec).headers(any());
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).onStatus(any(), any());
        verify(responseSpec).bodyToMono(JsonNode.class);
    }

    @Test
    void fetchTranslation_Success() {
        // Given
        long hotelId = 1270324L;
        String lang = "fr";
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(testResponse));

        // When & Then
        StepVerifier.create(cupidApiClient.fetchTranslation(hotelId, lang))
                .expectNext(testResponse)
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).bodyToMono(JsonNode.class);
    }

    @Test
    void fetchTranslation_Error() {
        // Given
        long hotelId = 1270324L;
        String lang = "fr";
        RuntimeException error = new RuntimeException("Translation fetch failed");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.error(error));

        // When & Then
        StepVerifier.create(cupidApiClient.fetchTranslation(hotelId, lang))
                .expectError(RuntimeException.class)
                .verify();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).bodyToMono(JsonNode.class);
    }

    @Test
    void fetchReviews_Success() {
        // Given
        long hotelId = 1270324L;
        int count = 10;
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(testResponse));

        // When & Then
        StepVerifier.create(cupidApiClient.fetchReviews(hotelId, count))
                .expectNext(testResponse)
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).bodyToMono(JsonNode.class);
    }

    @Test
    void fetchReviews_Error() {
        // Given
        long hotelId = 1270324L;
        int count = 10;
        RuntimeException error = new RuntimeException("Reviews fetch failed");
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.error(error));

        // When & Then
        StepVerifier.create(cupidApiClient.fetchReviews(hotelId, count))
                .expectError(RuntimeException.class)
                .verify();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersUriSpec).retrieve();
        verify(responseSpec).bodyToMono(JsonNode.class);
    }
}

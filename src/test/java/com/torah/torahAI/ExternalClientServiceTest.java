package com.torah.torahAI;

import com.torah.torahAI.data.documents.EmbeddingResponse;
import com.torah.torahAI.external.ExternalClientService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

class ExternalClientServiceTest {

    private ExternalClientService service;

    @BeforeEach
    void setup() {
        service = new ExternalClientService(null); // RestTemplate is unused
    }

    @Test
    void generateEmbedding_shouldReturnMono() {
        Mono<EmbeddingResponse> result = service.generateEmbedding("test query", "test-auth");
        assertNotNull(result);
    }

    @Test
    void generateQuery_shouldReturnMono() {
        Mono<String> result = service.generateQuery("test query", "kid-friendly", "test-auth");
        assertNotNull(result);
    }

    @Test
    void generateImageQuery_shouldReturnMono() {
        Mono<String> result = service.generateImageQuery("draw a star", "test-auth");
        assertNotNull(result);
    }
}
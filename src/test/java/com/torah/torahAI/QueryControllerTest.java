package com.torah.torahAI;

import com.torah.torahAI.controllers.QueryController;
import com.torah.torahAI.data.DataService;
import com.torah.torahAI.data.documents.BookOfEmbeddings;
import com.torah.torahAI.data.documents.EmbeddingResponse;
import com.torah.torahAI.external.ExternalClientService;
import com.torah.torahAI.model.QueryImageRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class QueryControllerTest {

    private ExternalClientService client;
    private DataService dataService;
    private ExecutorService virtualExecutor;
    private QueryController controller;

    @BeforeEach
    void setUp() {
        client = mock(ExternalClientService.class);
        dataService = mock(DataService.class);
        virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

        controller = new QueryController(client, dataService);
        controller.virtualExecutor = virtualExecutor; // set manually since it's autowired
    }

    @Test
    void testQuery_returnsMono() {
        String query = "What is the covenant?";
        String role = "kid-friendly";
        String auth = "Bearer test-token";

        EmbeddingResponse embedding = new EmbeddingResponse();
        BookOfEmbeddings doc = new BookOfEmbeddings();
        doc.setText("God made a covenant with Abraham.");

        when(client.generateEmbedding(eq(query), eq(auth))).thenReturn(Mono.just(embedding));
        when(dataService.findSimilarEmbeddings(any())).thenReturn(Mono.just(List.of(doc)));
        when(client.generateQuery(any(), eq(role), eq(auth))).thenReturn(Mono.just("response"));

        Mono<String> result = controller.query(query, role, auth);
        assertNotNull(result);
    }

    @Test
    void testQueryImage_returnsMono() throws Exception {
        String prompt = "Create an image of a menorah";
        String role = "kid-friendly";
        String auth = "Bearer test-token";
        QueryImageRequest query = new QueryImageRequest();
         query.setPrompt("draw a sexy starfish");
        query.setResponseFromCorrelatingPrompt("http://onlystar.com");

        when(client.generateImageQueries(eq(query), eq(auth))).thenReturn(Mono.just("image-url"));

        Mono<String> result = controller.queryImage(query, role, auth);
        assertNotNull(result);
    }
}
package com.torah.torahAI;

import com.torah.torahAI.data.DataService;
import com.torah.torahAI.data.documents.BookOfEmbeddings;
import com.torah.torahAI.data.documents.EmbeddingDataObject;
import com.torah.torahAI.data.documents.EmbeddingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataServiceTest {

    private DataService dataService;
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        dataService = new DataService(mongoTemplate);
    }

    @Test
    void testFindSimilarEmbeddings_returnsMono() {
        // Prepare fake EmbeddingResponse
        EmbeddingResponse response = new EmbeddingResponse();
        EmbeddingDataObject dataObject = new EmbeddingDataObject();
        dataObject.embedding = List.of(0.1f, 0.2f, 0.3f);
        response.data = List.of(dataObject);

        // Prepare mocked MongoDB aggregation return
        List<BookOfEmbeddings> dummyResults = List.of(new BookOfEmbeddings());
        AggregationResults<BookOfEmbeddings> aggResults = mock(AggregationResults.class);
        when(aggResults.getMappedResults()).thenReturn(dummyResults);
        when(mongoTemplate.aggregate(any(), eq("genesis_embeddings"), eq(BookOfEmbeddings.class)))
                .thenReturn(aggResults);

        Mono<List<BookOfEmbeddings>> result = dataService.findSimilarEmbeddings(Mono.just(response));
        assertNotNull(result);
    }
}
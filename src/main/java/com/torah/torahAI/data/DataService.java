package com.torah.torahAI.data;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

import com.torah.torahAI.data.documents.BookOfEmbeddings;
import com.torah.torahAI.data.documents.EmbeddingResponse;

@Service
public class DataService {

    private MongoTemplate mongoTemplate;

    @Autowired
    public DataService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<BookOfEmbeddings> findSimilarEmbeddings(EmbeddingResponse embedding) {
        // in atlas I built a search index (JSON policy) for the genesis_embedings
        // collection
        // mimic that same JSON policy here

        var embeddings = embedding.data.get(0).embedding;
        Document vectorSearchStage = new Document("$vectorSearch", new Document()
                .append("index", "vector_index") // your index name
                .append("queryVector", embeddings)
                .append("path", "embedding")
                .append("numCandidates", 100)
                .append("limit", 5)
                .append("similarity", "dotProduct"));

        Document projectStage = new Document("$project",
                new Document("_id", 1)
                        .append("text", 1)
                        .append("embedding", 1)
                        .append("score", new Document("$meta", "searchScore")) // optional, returns relevance
        );

        Aggregation aggregation = Aggregation.newAggregation(
                context -> vectorSearchStage,
                context -> projectStage);

        return mongoTemplate.aggregate(
                aggregation,
                "genesis_embeddings",
                BookOfEmbeddings.class).getMappedResults();

    }
}

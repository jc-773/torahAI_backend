package com.torah.torahAI.data;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

import com.torah.torahAI.data.documents.BookOfEmbeddings;
import com.torah.torahAI.data.documents.EmbeddingResponse;

import reactor.core.publisher.Mono;

@Service
public class DataService {

    private MongoTemplate mongoTemplate;

    @Autowired
    public DataService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Mono<List<BookOfEmbeddings>> findSimilarEmbeddings(Mono<EmbeddingResponse> embeddingMono) {
        return embeddingMono
                .map(embedding -> embedding.data.get(0).embedding)
                .flatMap(results -> {return mongoAtlasVectorSearch(results);
                });
    }

    private Mono<List<BookOfEmbeddings>>  mongoAtlasVectorSearch(List<Float> embeddingList) {
        return Mono.fromCallable(() -> {
               Document vectorSearchAgg = vectorSearchStage(embeddingList);
               Document projectStageAgg = projectStage(embeddingList);

                Aggregation aggregation = Aggregation.newAggregation(
                context -> vectorSearchAgg,
                context -> projectStageAgg);

                return mongoTemplate.aggregate(
                aggregation,
                "genesis_embeddings",
                BookOfEmbeddings.class).getMappedResults();
        });
    }

    private Document vectorSearchStage(List<Float> embeddingList) {
                var doc =  new Document("$vectorSearch", new Document()
                .append("index", "vector_index") // your index name
                .append("queryVector", embeddingList)
                .append("path", "embedding")
                .append("numCandidates", 100)
                .append("limit", 5)
                .append("similarity", "dotProduct"));
                return doc;
    }

    private Document projectStage(List<Float> embeddingList) {
                var doc = new Document("$project",
                new Document("_id", 1)
                        .append("text", 1)
                        .append("embedding", 1)
                        .append("score", new Document("$meta", "searchScore")));
                return doc;
    }
}

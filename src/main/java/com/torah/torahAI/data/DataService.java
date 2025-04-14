package com.torah.torahAI.data;

import java.util.List;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.stereotype.Service;

import com.mongodb.MongoNamespace;
import com.mongodb.internal.operation.AggregateOperation;
import com.torah.torahAI.data.documents.BookOfEmbeddings;
import com.torah.torahAI.data.repositories.QueryRepository;

@Service
public class DataService {
    
    private QueryRepository queryRepo;
    private MongoTemplate mongoTemplate;

    @Autowired
    public DataService(QueryRepository queryRepo, MongoTemplate mongoTemplate) {
        this.queryRepo = queryRepo;
        this.mongoTemplate = mongoTemplate;
    }

    public  List<BookOfEmbeddings> findSimilarEmbeddings(String embedding) {
       //in atlas I built a search index (JSON policy) for the genesis_embedings collection
       //mimic that same JSON policy here

       Document searchStage = new Document("$search", new Document()
       .append("index", "torah_vector_index")  // Use your actual index name
       .append("knnBeta", new Document()
           .append("vector", embedding)
           .append("path", "embedding")
           .append("k", 5)
           .append("similarity", "dotProduct"))
       );

       List<Document> pipeline = List.of(searchStage);

       return mongoTemplate.getDb()
               .getCollection("genesis_embeddings")
               .aggregate(pipeline, BookOfEmbeddings.class)
               .into(new java.util.ArrayList<>());
    }
}

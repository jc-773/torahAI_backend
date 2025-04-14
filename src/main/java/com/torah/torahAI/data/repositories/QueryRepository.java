package com.torah.torahAI.data.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.torah.torahAI.data.documents.BookOfEmbeddings;

@Repository
public interface QueryRepository extends MongoRepository<BookOfEmbeddings,org.bson.types.ObjectId> {
    List<BookOfEmbeddings>findBySource(String source);
}

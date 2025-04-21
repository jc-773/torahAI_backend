package com.torah.torahAI.data.documents;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "book_of_embeddings")
public class BookOfEmbeddings {
    @Id
    private ObjectId id;
    private String text;
    private List<Float> embedding;
    private String source;

    public ObjectId getId() {
        return id;
    }
    public void setId(ObjectId id) {
        this.id = id;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public List<Float> getEmbedding() {
        return embedding;
    }
    public void setEmbedding(List<Float> embedding) {
        this.embedding = embedding;
    }
    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }
}

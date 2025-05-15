package com.torah.torahAI.controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.torah.torahAI.data.DataService;
import com.torah.torahAI.data.documents.BookOfEmbeddings;
import com.torah.torahAI.data.documents.EmbeddingResponse;
import com.torah.torahAI.external.ExternalClientService;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
public class QueryController {
    private static final Logger log = LoggerFactory.getLogger(QueryController.class);

    @Autowired
    private ExecutorService virtualExecutor;
    private ExternalClientService client;
    private DataService dataService;

    @Autowired
    public QueryController(ExternalClientService client, DataService dataService) {
        this.client = client;
        this.dataService = dataService;
    }
    
    @PostMapping("/query")
    public String query(@RequestBody String query, @RequestParam String role) throws InterruptedException, ExecutionException {
        CompletableFuture<EmbeddingResponse> futureEmbedding = CompletableFuture.supplyAsync(() -> {
            return  client.generateEmbedding(query);
        });
        var embedding = futureEmbedding.get();
        CompletableFuture<List<BookOfEmbeddings>> futureVectorSearch = CompletableFuture.supplyAsync(() -> {
                return dataService.findSimilarEmbeddings(embedding);
        }, virtualExecutor);
        var results = futureVectorSearch.get();
        var contextText = appendText(results);
        var prompt = queryTextFromListOfEmbeddings(contextText, query);

        CompletableFuture<String> futureQuery = CompletableFuture.supplyAsync(() -> {
             return client.generateQuery(prompt, role);
        });
        var response = futureQuery.get();
        return response;
    }

    @PostMapping(value = "/query/image")
    public String queryImage(@RequestBody String prompt, @RequestParam String role) throws InterruptedException, ExecutionException {
        CompletableFuture<String> futureImage = CompletableFuture.supplyAsync(() -> {
             return client.generateImageQuery(prompt);
        });
        return futureImage.get();
    }

    private StringBuilder appendText(List<BookOfEmbeddings> futureEmbedding) {
        StringBuilder contextText = new StringBuilder();
        for(BookOfEmbeddings doc : futureEmbedding) {
            contextText.append("- ").append(doc.getText()).append("\n");
        }
        return contextText;
    }

    private String queryTextFromListOfEmbeddings(StringBuilder contextText, String query) {
        
        //final prompt string with context and query
        return String.format("""
        You're a friendly Jewish centric teacher with focus on covenant, law, and Jewish identity while explaining the Torah to a child.
        
        Context:
        %s
        Question:
        %s
        
        Answer in a way that's simple, clear, and easy for a kid to understand:
        """, contextText.toString().trim(), query);
    }
}

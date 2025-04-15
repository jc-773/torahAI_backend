package com.torah.torahAI.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torah.torahAI.data.DataService;
import com.torah.torahAI.data.documents.BookOfEmbeddings;
import com.torah.torahAI.external.ExternalClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
public class QueryController {

    private static final Logger log = LoggerFactory.getLogger(QueryController.class);

    private ExternalClientService client;
    private DataService dataService;
    JSONArray messages = new JSONArray();

    @Autowired
    public QueryController(ExternalClientService client, DataService dataService) {
        log.info("QueryController class is initialized");
        this.client = client;
        this.dataService = dataService;
    }
    
    @PostMapping("/query")
    public String query(@RequestBody String query) {

        Map<String, Object> message = Map.of(
            "role","user",
            "content",query
        );
        Map<String, Object> requestBody = Map.of(
            "model","gpt-4",
            "message",List.of(message)
        );

        var embedding = client.generateEmbedding(requestBody);
        var getListOfEmbeddings = dataService.findSimilarEmbeddings(embedding);
        StringBuilder contextText = new StringBuilder();
        for(BookOfEmbeddings doc : getListOfEmbeddings) {
            contextText.append("- ").append(doc.getText()).append("\n");
            var prompt = queryTextFromListOfEmbeddings(contextText, query);
            client.generateQuery(prompt);
        }
        return null;
    }

    private String queryTextFromListOfEmbeddings(StringBuilder contextText, String query) {
        
        // 👇 Construct the final prompt string
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

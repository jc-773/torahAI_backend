package com.torah.torahAI.controllers;

import java.util.Map;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.torah.torahAI.data.DataService;
import com.torah.torahAI.data.documents.BookOfEmbeddings;
import com.torah.torahAI.external.ExternalClientService;
import com.torah.torahAI.responses.QueryWithImageResponse;

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
    public QueryWithImageResponse query(@RequestBody String query, @RequestParam String role) {
        QueryWithImageResponse responseObject = new QueryWithImageResponse();

        //TODO: Why?
        client.getAllBooks();

        //TODO: Blocking call
        //generate embeddings for semantic meaning of the query
        var embedding = client.generateEmbedding(query);

        //TODO: Blocking call
        //takes those embeddings and do a vector search in a mongoDB ATLAS
        var getListOfEmbeddings = dataService.findSimilarEmbeddings(embedding);
        StringBuilder contextText = new StringBuilder();

        //append what is found
        for(BookOfEmbeddings doc : getListOfEmbeddings) {
            contextText.append("- ").append(doc.getText()).append("\n");
        }
        //generate prompt for openAI
        var prompt = queryTextFromListOfEmbeddings(contextText, query);

        //TODO: Both blocking calls
        responseObject.queryResponse = client.generateQuery(prompt, role);
        responseObject.imageResponse = client.generateImageQuery(prompt);

        //send prompt to openAI and return the response
        return responseObject;
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

package com.torah.torahAI.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
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

    private ExternalClientService client;
    private DataService dataService;
    JSONArray messages = new JSONArray();

    @Autowired
    public QueryController(ExternalClientService client, DataService dataService) {
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

        for(BookOfEmbeddings doc : getListOfEmbeddings) {
        }

        return null;
    }
  

   
    
}

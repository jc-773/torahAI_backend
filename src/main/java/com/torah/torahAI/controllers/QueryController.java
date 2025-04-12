package com.torah.torahAI.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.torah.torahAI.external.ExternalClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class QueryController {

    private final String URL = "https://api.openai.com/v1/embeddings";

    private ExternalClientService client;

    @Autowired
    public QueryController(ExternalClientService client) {
        this.client = client;
    }
    
    @GetMapping("/query")
    public String queryEndpoint(@RequestBody String query) {
        
    }
}

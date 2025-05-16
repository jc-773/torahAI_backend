package com.torah.torahAI.controllers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.torah.torahAI.Utils;
import com.torah.torahAI.data.DataService;
import com.torah.torahAI.external.ExternalClientService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
public class QueryController {
    private static final Logger log = LoggerFactory.getLogger(QueryController.class);

    private ExternalClientService client;
    private DataService dataService;

    @Autowired
    public QueryController(ExternalClientService client, DataService dataService) {
        this.client = client;
        this.dataService = dataService;
    }
    
    @PostMapping("/query") //if the I/O call can invoke an exception, then run it with fromCallable on its own virtual thread
    public Mono<String> query(@RequestBody String query, @RequestParam String role) { 
        return Mono.fromCallable(() -> client.generateEmbedding(query)) 
        .subscribeOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor()))
        .flatMap(embeddings -> 
                    Mono.fromCallable(() -> dataService.findSimilarEmbeddings(embeddings))
                    .subscribeOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor()))
        .map(results -> Utils.appendText(results))
        .map(context -> Utils.setContextForPrompt(context, query))
        .flatMap(prompt -> 
                    Mono.fromCallable(() -> client.generateQuery(prompt, role)))
                    .subscribeOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor())));
    }

    @PostMapping(value = "/query/image")
    public Mono<String> queryImage(@RequestBody String prompt, @RequestParam String role) throws InterruptedException, ExecutionException { 
        return Mono.fromCallable(() -> client.generateImageQuery(prompt))
            .subscribeOn(Schedulers.fromExecutor(Executors.newVirtualThreadPerTaskExecutor()));
    }
}

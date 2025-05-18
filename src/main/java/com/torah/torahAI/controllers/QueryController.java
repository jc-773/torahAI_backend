package com.torah.torahAI.controllers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
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
import org.springframework.web.bind.annotation.RequestHeader;
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
    
    @PostMapping("/query") //if the I/O call can invoke an exception, then run it with fromCallable on its own virtual thread
    public Mono<String> query(@RequestBody String query, @RequestParam String role, @RequestHeader("Authorization") String Auth) { 
        return Mono.fromCallable(() -> client.generateEmbedding(query,Auth))
        .subscribeOn(Schedulers.fromExecutor(virtualExecutor))
        .flatMap(embedding -> dataService.findSimilarEmbeddings(embedding))
        .map(Utils::appendText)
        .map(context -> Utils.setContextForPrompt(context, query))
        .flatMap(prompt -> client.generateQuery(prompt, role,Auth))
        .map(Utils:: mapResponse);
    }

    @PostMapping(value = "/query/image")
    public Mono<String> queryImage(@RequestBody String prompt, @RequestParam String role, @RequestHeader("Authorization") String Auth) throws InterruptedException, ExecutionException { 
       return client.generateImageQuery(prompt, Auth)
        .map(Utils::mapImageResponse)
        .subscribeOn(Schedulers.fromExecutor(virtualExecutor));
    }
}

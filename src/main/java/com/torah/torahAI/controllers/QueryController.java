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
import com.torah.torahAI.data.documents.EmbeddingResponse;
import com.torah.torahAI.external.ExternalClientService;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping
public class QueryController {
    private static final Logger log = LoggerFactory.getLogger(QueryController.class);
   
    @Autowired
    public ExecutorService virtualExecutor;
    private ExternalClientService client;
    private DataService dataService;

    @Autowired
    public QueryController(ExternalClientService client, DataService dataService) {
        this.client = client;
        this.dataService = dataService;
    }

    @PostMapping("/query")
    public Mono<String> query(@RequestBody String query, @RequestParam String role, @RequestHeader("Authorization") String auth) {
          return Mono.defer(() -> client.generateEmbedding(query, auth)
            .flatMap(embedding -> dataService.findSimilarEmbeddings(Mono.just(embedding)))
            .subscribeOn(Schedulers.fromExecutor(virtualExecutor))
            .map(Utils::appendText)
            .map(context -> Utils.setContextForPrompt(context, query))
            .flatMap(prompt -> client.generateQuery(prompt, role, auth))
            .map(Utils::mapResponse)
            .doOnNext(e -> log.info("mapResponse: {}", e))
            .doOnError(e -> log.error("following exception occured: {}",e))
        ).subscribeOn(Schedulers.fromExecutor(virtualExecutor));
    }

    @PostMapping(value = "/query/image")
    public Mono<String> queryImage(@RequestBody String prompt, @RequestParam String role, @RequestHeader("Authorization") String Auth) throws InterruptedException, ExecutionException { 
       return client.generateImageQuery(prompt, Auth)
        .subscribeOn(Schedulers.fromExecutor(virtualExecutor))
        .map(Utils::mapImageResponse);
    }
}

package com.torah.torahAI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class TorahAiApplication {

	@Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

	@Bean 
	public ExecutorService mainExecutorService() {
		return Executors.newVirtualThreadPerTaskExecutor();
	}

	public static void main(String[] args) {
		SpringApplication.run(TorahAiApplication.class, args);
	}
}

package com.torah.torahAI.external;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalClientService {

    private final String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public ExternalClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateEmbeddings(String embedding) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("input", embedding);
        body.put("model", "text-embedding-3-small" );

        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(body, headers);
        
        var response = restTemplate.exchange(EMBEDDING_URL, HttpMethod.GET, entity, String.class);
        if(response.hasBody()) {
            return response.getBody();
        } else {
            return null;
        }
    }
}

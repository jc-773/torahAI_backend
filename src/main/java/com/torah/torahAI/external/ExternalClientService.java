package com.torah.torahAI.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.torah.torahAI.data.documents.BookOfEmbeddings;

@Service
public class ExternalClientService {

    private final String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";
    private final String PROMPT_URL = "https://api.openai.com/v1/prompt";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public ExternalClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateEmbedding(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(requestBody, headers);
        var response = restTemplate.exchange(EMBEDDING_URL, HttpMethod.GET, entity, String.class);
        if(response.hasBody()) {
            return response.getBody();
        } else {
            return null;
        }
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

    public String generateQuery(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var requestBody = buildQuery(query);
        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(requestBody, headers);
        
        var response = restTemplate.exchange(PROMPT_URL, HttpMethod.POST, entity, String.class);
        if(response.hasBody()) {
            return response.getBody();
        } else {
            return null;
        }
    }

    private Map<String, Object> buildQuery(String query) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
            "key","system",
            "content", "You are a friendly Jewish centric teacher with focus on covenant, law, and Jewish identity while explaining the Torah to a child"
        ));
        messages.add(Map.of(
            "key","user",
            "content",query
        ));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

      return requestBody;
    }
}

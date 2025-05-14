package com.torah.torahAI.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.torah.torahAI.Utils;
import com.torah.torahAI.data.documents.EmbeddingResponse;

@Service
public class ExternalClientService {

    private static final Logger log = LoggerFactory.getLogger(ExternalClientService.class);

    private final String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";
    private final String PROMPT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String BOOK_URL_PARAMTERIZED = "https://www.sefaria.org/api/texts/%s.1?lang=english";
    private final String OPENAI_API_KEY = Optional.ofNullable(System.getenv("OPENAI_API_KEY"))
    .orElseThrow(() -> new IllegalStateException("Missing OPENAI_API_KEY environment variable"));

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    public ExternalClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public EmbeddingResponse generateEmbedding(String query) {
        var requestBody = buildRequestBody(query);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + OPENAI_API_KEY);
        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(requestBody, headers);
        var response = restTemplate.exchange(EMBEDDING_URL, HttpMethod.POST, entity, EmbeddingResponse.class);
        if(response.hasBody()) {
            return response.getBody();
        } else {
            return null;
        }
    }

    public String generateQuery(String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + OPENAI_API_KEY);
        var requestBody = buildQuery(query);
        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(requestBody, headers);
        
        var response = restTemplate.exchange(PROMPT_URL, HttpMethod.POST, entity, String.class);
       
        return Utils.mapResponse(response);
    }

    private  Map<String, Object> buildRequestBody(String query) {
        return  Map.of(
            "model","text-embedding-3-small",
            "input",query,
            "encoding_format", "float"
        );
    }

    private Map<String, Object> buildQuery(String query) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
            "role","system",
            "content", "You are a friendly Jewish centric teacher with focus on covenant, law, and Jewish identity while explaining the Torah to a child"
        ));
        messages.add(Map.of(
            "role","user",
            "content",query
        ));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 100);

      return requestBody;
    }

    public String getAllBooks() {
        List<String> listOfBooks = new ArrayList<>();
        listOfBooks.add("Genesis");
        listOfBooks.add("Exodus");
        listOfBooks.add("Leviticus");
        listOfBooks.add("Numbers");
        listOfBooks.add("Deuteronomy");

        int i = 0;
        while(i < listOfBooks.size()) {
            try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                String book = listOfBooks.get(i);
                String url = String.format(BOOK_URL_PARAMTERIZED, book);
                executor.submit(() -> {
                var response = restTemplate.exchange(url,HttpMethod.GET,HttpEntity.EMPTY,String.class);
                String filteredResponse = Utils.mapResponse(response);
                log.info("response: {}", filteredResponse);
                return Utils.mapResponse(response);
                });
            }
            i++;
        }
        return null;
    }
}

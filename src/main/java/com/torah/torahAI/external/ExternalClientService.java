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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.torah.torahAI.Utils;
import com.torah.torahAI.data.documents.EmbeddingResponse;

@Service
public class ExternalClientService {

    private static final Logger log = LoggerFactory.getLogger(ExternalClientService.class);

    private final String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";
    private final String PROMPT_URL = "https://api.openai.com/v1/chat/completions";
    private final String BOOK_URL_PARAMTERIZED = "https://www.sefaria.org/api/texts/%s.1?lang=english";
    private final String IMAGE_GENERATION_URL = "https://api.openai.com/v1/images/generations";
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

    public String generateQuery(String query, String role) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + OPENAI_API_KEY);
        var requestBody = buildQuery(query, role);
        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(requestBody, headers);
        
        var response = restTemplate.exchange(PROMPT_URL, HttpMethod.POST, entity, String.class);
       
        return Utils.mapResponse(response);
    }

  public String generateImageQuery(String prompt) {

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(OPENAI_API_KEY);

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("model", "dall-e-3");
    requestBody.put("prompt", "Create a kid-friendly, cartoon-style image featuring Jewish cultural or Torah-inspired elements. All characters should have brown skin tones. Base the image on the following prompt:" + prompt);
    requestBody.put("size", "1024x1024");
     //requestBody.put("n", "2");
    requestBody.put("quality", "standard");

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

    try {
        ResponseEntity<String> response = restTemplate.exchange(
            IMAGE_GENERATION_URL,
            HttpMethod.POST,
            entity,
            String.class
        );
        return Utils.mapImageResponse(response);
    } catch (HttpStatusCodeException e) {
        log.error("OpenAI API error: {}", e.getResponseBodyAsString());
        return "Image generation failed: " + e.getStatusCode();
    }


}

    private  Map<String, Object> buildRequestBody(String query) {
        return  Map.of(
            "model","text-embedding-3-small",
            "input",query,
            "encoding_format", "float"
        );
    }

    private Map<String, Object> buildQuery(String query, String role) {
        List<Map<String, String>> messages = new ArrayList<>();
        String content = "";
        if(role.equalsIgnoreCase("kid-friendly")) {
            content = "You are a warm and kid-friendly Torah teacher who explains Jewish concepts to children. Focus on the themes of covenant, law, and Jewish identity. Use simple language, relatable examples, and a friendly tone to help young learners understand the Torah.";
        } else if(role.equalsIgnoreCase("scholaraly")) {
            content = "You are a scholarly Jewish Torah teacher who explains concepts with intellectual depth and precision. Focus on the themes of covenant, Jewish law (halakha), and Jewish identity. Use sophisticated language and well-reasoned explanations appropriate for an educated audience interested in deep Torah study.";
        } 
        else if(role.equalsIgnoreCase("storyteller")) {
            content = "You are a Jewish Torah teacher who teaches through immersive storytelling. Explain the stories and teachings of the Torah in a rich, detailed, and engaging narrative style with no less than two paragraphs at all times. Focus especially on the themes of covenant, law, and Jewish identity. Use vivid language, emotional depth, and character-driven descriptions to bring the stories to life. Include thoughtful comparisons to modern-day situations, people, or challenges so the listener can connect ancient lessons to the present day. Prioritize long, detailed explanations that unfold like a story being told to an attentive audience.";
        } 
        messages.add(Map.of(
            "role","assistant",
            "content", content
        ));
        messages.add(Map.of(
            "role","assistant",
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

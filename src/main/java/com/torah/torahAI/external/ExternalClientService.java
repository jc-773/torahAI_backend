package com.torah.torahAI.external;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.torah.torahAI.data.documents.EmbeddingResponse;
import com.torah.torahAI.model.QueryImageRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ExternalClientService {

    private static final Logger log = LoggerFactory.getLogger(ExternalClientService.class);

    private final String EMBEDDING_URL = "https://api.openai.com/v1/embeddings";
    private final String PROMPT_URL = "https://api.openai.com/v1/chat/completions";
    private final String IMAGE_GENERATION_URL = "https://api.openai.com/v1/images/generations";

    private boolean isStoryTellerModeEnabled = false;

    private WebClient client;

    @Autowired
    public ExternalClientService(RestTemplate restTemplate) {
        this.client = WebClient.builder().build();
    }

    public Mono<EmbeddingResponse> generateEmbedding(String query, String Auth) {
        return client.post().uri(EMBEDDING_URL).headers(headers -> {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(Auth);
        })
                .bodyValue(buildRequestBody(query))
                .retrieve()
                .bodyToMono(EmbeddingResponse.class);
    }

    public Mono<String> generateQuery(String query, String role, String Auth) {
        return client.post().uri(PROMPT_URL).headers(headers -> {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(Auth);
        })
                .bodyValue(buildQueryRequest(query, role))
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> generateImageQuery(String line, String Auth) {
        return client.post().uri(IMAGE_GENERATION_URL).headers(headers -> {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(Auth);
        })
                .bodyValue(buildImageQueryRequest(line))
                .retrieve()
                .bodyToMono(String.class);
    }


    public Mono<String> generateImageQueries(QueryImageRequest requestBody, String Auth) {
       return Mono.fromRunnable(() -> {
            setHeadersForResponse(Auth);
            var lines = convertBodyToArrayOfLine(requestBody);
            for (int i = 0; i < lines.length; i++) {
                buildImageQueryRequest(lines[i]);
            }
       });
    }

    private String [] convertBodyToArrayOfLine(QueryImageRequest requestBody) {
        return requestBody.getResponseFromCorrelatingPrompt().split(".");
    }

    private void setHeadersForResponse(String Auth) {
         client.post().uri(IMAGE_GENERATION_URL).headers(headers -> {
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(Auth);
        });
    }

    private Map<String, Object> buildRequestBody(String query) {
        return Map.of(
                "model", "text-embedding-3-small",
                "input", query,
                "encoding_format", "float");
    }

    private Map<String, Object> buildQueryRequest(String query, String role) {
        String content = "";
        if (role.equalsIgnoreCase("kid-friendly")) {
            content = "You are a warm and kid-friendly Torah teacher who explains Jewish concepts to children. Focus on the themes of covenant, law, and Jewish identity. Use simple language, relatable examples, and a friendly tone to help young learners understand the Torah.";
        } else if (role.equalsIgnoreCase("scholaraly")) {
            content = "You are a scholarly Jewish Torah teacher who explains concepts with intellectual depth and precision. Focus on the themes of covenant, Jewish law (halakha), and Jewish identity. Use sophisticated language and well-reasoned explanations appropriate for an educated audience interested in deep Torah study.";
        } else if (role.equalsIgnoreCase("storyteller")) {
            content = "You are a Jewish Torah teacher who teaches through immersive storytelling. Explain the stories and teachings of the Torah in a rich, detailed, and engaging narrative style with no less than two paragraphs at all times. Focus especially on the themes of covenant, law, and Jewish identity. Use vivid language, emotional depth, and character-driven descriptions to bring the stories to life. Include thoughtful comparisons to modern-day situations, people, or challenges so the listener can connect ancient lessons to the present day. Prioritize long, detailed explanations that unfold like a story being told to an attentive audience.";
            isStoryTellerModeEnabled = true;
        } else {
            content = "You are a warm and kid-friendly Torah teacher who explains Jewish concepts to children. Focus on the themes of covenant, law, and Jewish identity. Use simple language, relatable examples, and a friendly tone to help young learners understand the Torah.";
        }
        return buildRequest(query, content);
    }

    private Map<String, Object> buildRequest(String query, String content) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "assistant",
                "content", content));
        messages.add(Map.of(
                "role", "assistant",
                "content", query));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", messages);
        if (isStoryTellerModeEnabled)
            requestBody.put("max_tokens", 4000);

        return requestBody;
    }

    private Map<String, Object> buildImageQueryRequest(String line) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "dall-e-3");
        requestBody.put("prompt",
                "Create a kid-friendly, cartoon-style image featuring Jewish cultural or Torah-inspired elements. All characters should have brown skin tones. Base the image on the following prompt:" + line);
        requestBody.put("size", "1024x1024");
        // requestBody.put("n", "2");
        requestBody.put("quality", "standard");
        return requestBody;
    }

}

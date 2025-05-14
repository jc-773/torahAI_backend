package com.torah.torahAI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {
    private static Logger log = LoggerFactory.getLogger(Utils.class);

    public static String mapResponse(ResponseEntity<String> response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("response body: " + response.getBody());
            JsonNode root = mapper.readTree(response.getBody());
    
             JsonNode contentNode = root
            .path("choices")
            .path(0)
            .path("message")
            .path("content");

        return contentNode.asText().trim();
        } catch (Exception e) {
            log.error("oops... having trouble mapping the response", e);
        }
        return null;
    }
}

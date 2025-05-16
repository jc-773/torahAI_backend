package com.torah.torahAI;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.torah.torahAI.data.documents.BookOfEmbeddings;

public class Utils {
    private static Logger log = LoggerFactory.getLogger(Utils.class);

    public static long timer(Runnable run) {
        var start = System.currentTimeMillis();
        run.run();
        var end = System.currentTimeMillis();
        return end - start;
    }

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

    public static String mapImageResponse(ResponseEntity<String> response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            System.out.println("response body: " + response.getBody());
            JsonNode root = mapper.readTree(response.getBody());
    
             JsonNode contentNode = root
            .path("data")
            .get(0)           // get first object in the array
            .path("url");   

        return contentNode.asText().trim();
        } catch (Exception e) {
            log.error("oops... having trouble mapping the response", e);
        }
        return null;
    }


    public static StringBuilder appendText(List<BookOfEmbeddings> futureEmbedding) {
        StringBuilder contextText = new StringBuilder();
        for(BookOfEmbeddings doc : futureEmbedding) {
            contextText.append("- ").append(doc.getText()).append("\n");
        }
        return contextText;
    }

    public static String setContextForPrompt(StringBuilder contextText, String query) {
        
        //final prompt string with context and query
        return String.format("""
        You're a friendly Jewish centric teacher with focus on covenant, law, and Jewish identity while explaining the Torah to a child.
        
        Context:
        %s
        Question:
        %s
        
        Answer in a way that's simple, clear, and easy for a kid to understand:
        """, contextText.toString().trim(), query);
    }
}

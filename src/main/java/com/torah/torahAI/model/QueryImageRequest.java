package com.torah.torahAI.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryImageRequest {

    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("responseFromCorrelatingPrompt")
    private String responseFromCorrelatingPrompt;

    // Default constructor
    public QueryImageRequest() {}

    // All-args constructor
    public QueryImageRequest(String prompt, String responseFromCorrelatingPrompt) {
        this.prompt = prompt;
        this.responseFromCorrelatingPrompt = responseFromCorrelatingPrompt;
    }

    // Getters and setters
    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResponseFromCorrelatingPrompt() {
        return responseFromCorrelatingPrompt;
    }

    public void setResponseFromCorrelatingPrompt(String responseFromCorrelatingPrompt) {
        this.responseFromCorrelatingPrompt = responseFromCorrelatingPrompt;
    }

    @Override
    public String toString() {
        return "PromptResponse{" +
                "prompt='" + prompt + '\'' +
                ", responseFromCorrelatingPrompt='" + responseFromCorrelatingPrompt + '\'' +
                '}';
    }
}

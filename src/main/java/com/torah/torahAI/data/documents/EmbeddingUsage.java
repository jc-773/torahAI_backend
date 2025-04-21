package com.torah.torahAI.data.documents;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmbeddingUsage {
    @JsonProperty("prompt_token")
    public String promptToken;

    @JsonProperty("total_tokens")
    public String total_tokens;

}

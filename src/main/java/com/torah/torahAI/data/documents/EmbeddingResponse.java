package com.torah.torahAI.data.documents;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmbeddingResponse {
    @JsonProperty("object")
    public String object;

    @JsonProperty("data")
    public List<EmbeddingDataObject> data;

    @JsonProperty("model")
    public String model;

    @JsonProperty("usage")
    public EmbeddingUsage usage;
}

package com.torah.torahAI.data.documents;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmbeddingDataObject {
    @JsonProperty("object")
    public String object;

    @JsonProperty("index")
    public int index;

    @JsonProperty("embedding")
    public List<Float> embedding;
}

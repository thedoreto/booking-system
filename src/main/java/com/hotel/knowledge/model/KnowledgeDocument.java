package com.hotel.knowledge.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document(collection = "knowledge")
public class KnowledgeDocument {

    @Id
    private String id;
    private String text;
    private List<Double> embedding;
    private Map<String, Object> metadata;

    public KnowledgeDocument() {
        // REQUIRED by Spring Data Mongo
    }

    public String getText() { return text; }
    public List<Double> getEmbedding() { return embedding; }
    public Map<String, Object> getMetadata() { return metadata; }

    public void setText(String text) { this.text = text; }

    public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }

    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
package com.hotel.hotelinfo.dto;

import java.util.List;

public class KnowledgeDTO {

    private String title;
    private String text;
    private String category;
    private List<String> tags;
    private String source;

    public KnowledgeDTO() {
    }

    public KnowledgeDTO(String title,
                        String text,
                        String category,
                        List<String> tags,
                        String source) {

        this.title = title;
        this.text = text;
        this.category = category;
        this.tags = tags;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
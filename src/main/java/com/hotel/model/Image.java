package com.hotel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "images")
public class Image {

    @Id
    private String id;

    private String url;

    private String title;

    public Image() {
        // REQUIRED by Spring Data Mongo
    }

    public Image(String id, String url, String title) {
        this.id = id;
        this.url = url;
        this.title = title;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getUrl() { return url;}

    public void setUrl(String url) { this.url = url; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }
}

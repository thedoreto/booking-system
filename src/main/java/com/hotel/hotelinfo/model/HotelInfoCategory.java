package com.hotel.hotelinfo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "hotelinfo_category")
public class HotelInfoCategory {

    @Id
    private String id;
    private String name;
    private String title;
    private boolean required;
    private int order;

    public HotelInfoCategory() {
        // REQUIRED by Spring Data Mongo
    }

    public int getOrder() { return order; }
    public boolean isRequired() { return required; }
    public String getTitle() { return title; }
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }
    public void setTitle(String title) { this.title = title; }
    public void setRequired(boolean required) { this.required = required; }

    public void setOrder(int order) {
        this.order = order;
    }
}

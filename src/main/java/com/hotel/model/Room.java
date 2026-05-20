package com.hotel.model;

import com.hotel.model.enums.RoomType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "rooms")
public class Room {

    @Id
    private String id;

    private int roomNumber;
    private RoomType type;  // SINGLE, DOUBLE, APARTMENT
    private double pricePerNight;
    private List<String> images = new ArrayList<>();

    public Room() {
        // REQUIRED by Spring Data Mongo
    }

    public Room(int roomNumber, RoomType type, double pricePerNight, List<String> images) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.images = images;
    }

    public String getId() {
        return id;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomType getType() {
        return type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

//    public void setId(String id) { this.id = id;}

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public List<String> getImages() { return images; }

    public void setImages(List<String> images) { this.images = images; }

    public void addImage(String aImage) {
        images.add(aImage);
    }
}
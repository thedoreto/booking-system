package com.hotel.dto;

import java.util.ArrayList;
import java.util.List;

public class RoomDTO {

    private String id;
    private int roomNumber;
    private String type;//SINGLE, DOUBLE, APARTMENT
    private double pricePerNight;
    private List<String> images = new ArrayList<>();

    public RoomDTO(String id, int roomNumber, String type, double pricePerNight, List<String> images) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.images = images;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public List<String> getImages() { return images; }

    public void setImages(List<String> images) { this.images = images; }
}

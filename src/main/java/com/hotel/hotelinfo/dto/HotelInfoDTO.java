package com.hotel.hotelinfo.dto;

import java.util.List;

// Presentation for main page - coming from hotel_info collection
public class HotelInfoDTO {

    private String name;
    private String description;
    private String address;
    private String phone;
    private String email;
    private List<String> imageIds;

    public HotelInfoDTO() {
    }

    public HotelInfoDTO(String name,
                        String description,
                        String address,
                        String phone,
                        String email,
                        List<String> imageIds) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.imageIds = imageIds;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<String> imageIds) {
        this.imageIds = imageIds;
    }
}
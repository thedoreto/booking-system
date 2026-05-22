package com.hotel.dto;

import java.time.LocalDate;

public class BookingDTO {
    private String id;
    private String userId;
    private String userName;
    private String roomId;
    private String roomNumber;
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long nights;
    private double totalPrice;
    private String status;//CONFIRMED, CANCELED

    public BookingDTO(String id, String userId, String userName, String roomId, String roomNumber,
                      String roomType, LocalDate checkInDate, LocalDate checkOutDate, long nights,
                      double totalPrice, String status) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.nights = nights;
        this.totalPrice = totalPrice;
        this.status = status;
    }



    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRoomId() {
        return roomId;
    }
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    public LocalDate getCheckInDate() {
        return checkInDate;
    }
    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }
    public LocalDate getCheckOuDate() { return checkOutDate;  }
    public void setCheckOuDate(LocalDate checkOuDate) {
        this.checkOutDate = checkOuDate;
    }
    public long getNights() {
        return nights;
    }
    public void setNights(long nights) {
        this.nights = nights;
    }
    public double getTotalPrice() {
        return totalPrice;
    }
    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getUserName() { return userName;}
    public void setUserName(String userName) { this.userName = userName; }
    public String getRoomNumber() { return roomNumber;  }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
}

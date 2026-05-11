package com.hotel.dto;

import java.time.LocalDate;

public class BookingDTO {
    private String id;
    private String customerId;
    private String customerName;
    private String roomId;
    private String roomNumber;
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private long nights;
    private double totalPrice;
    private String status;//CONFIRMED, CANCELED

    public BookingDTO(String id, String customerId, String customerName, String roomId, String roomNumber,
                      String roomType, LocalDate checkInDate, LocalDate checkOutDate, long nights,
                      double totalPrice, String status) {
        System.out.println("BookingDTO constructor called with id: " + id);
        this.id = id;
        System.out.println("BookingDTO constructor called with customerId: " + customerId);
        this.customerId = customerId;
        System.out.println("BookingDTO constructor called with customerName: " + customerName);
        this.customerName = customerName;
        System.out.println("BookingDTO constructor called with roomId: " + roomId);
        this.roomId = roomId;
        System.out.println("BookingDTO constructor called with roomNumber: " + roomNumber);
        this.roomNumber = roomNumber;
        System.out.println("BookingDTO constructor called with roomType: " + roomType);
        this.roomType = roomType;
        System.out.println("BookingDTO constructor called with checkInDate: " + checkInDate);
        this.checkInDate = checkInDate;
        System.out.println("BookingDTO constructor called with checkOutDate: " + checkOutDate);
        this.checkOutDate = checkOutDate;
        System.out.println("BookingDTO constructor called with nights: " + nights);
        this.nights = nights;
        System.out.println("BookingDTO constructor called with totalPrice: " + totalPrice);
        this.totalPrice = totalPrice;
        System.out.println("BookingDTO constructor called with status: " + status);
        this.status = status;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

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

    public LocalDate getCheckOuDate() {
        return checkOutDate;
    }

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

    public String getCustomerName() { return customerName;}

    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getRoomNumber() { return roomNumber;  }

    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }

    public void setRoomType(String roomType) { this.roomType = roomType; }

    public LocalDate getCheckOutDate() { return checkOutDate; }

    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
}

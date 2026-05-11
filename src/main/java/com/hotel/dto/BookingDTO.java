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
    //    System.out.printf("BookingDTO constructor - customerId: %s, customerName: %s, roomId: %s, roomNumber: %s, " +
      //          "roomType: %s, checkInDate: %s, checkOutDate: %s, nights: %d, totalPrice: %.2f, status: %s%n " +
        //        customerId, customerName, roomId, roomNumber, roomType, checkInDate.toString(),
          //              checkOutDate.toString(), nights, totalPrice, status);
        this.customerId = customerId;
        this.customerName = customerName;
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

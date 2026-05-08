package com.hotel.dto;

import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.hotel.model.enums.BookingStatus;

import java.time.LocalDate;

public class BookingDTO {
    private long id;
    private String customerId;
    private String roomId;
    private LocalDate checkInDate;
    private LocalDate checkOuDate;
    private long nights;
    private double totalPrice;
    private String status;//CONFIRMED, CANCELED

    public BookingDTO(long id, String customerId, String roomId, LocalDate checkInDate, LocalDate checkOuDate, long nights, double totalPrice, String status) {
        System.out.println("Booking id: " + id);
        this.id = id;
        this.customerId = customerId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOuDate = checkOuDate;
        this.nights = nights;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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
        return checkOuDate;
    }

    public void setCheckOuDate(LocalDate checkOuDate) {
        this.checkOuDate = checkOuDate;
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
}

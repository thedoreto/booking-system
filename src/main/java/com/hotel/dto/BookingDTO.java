package com.hotel.dto;

import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.hotel.model.enums.BookingStatus;

import java.time.LocalDate;

public class BookingDTO {
    private long id;
    private int customerId;
    private int roomId;
    private LocalDate checkInDate;
    private LocalDate checkOuDate;
    private long nights;
    private double totalPrice;
    private String status;//CONFIRMED, CANCELED

    public BookingDTO(long id, int customerId, int roomId, LocalDate checkInDate, LocalDate checkOuDate, long nights, double totalPrice, String status) {
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

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
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

package com.hotel.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hotel.model.enums.BookingStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Document(collection = "bookings")
public class Booking {

    @Id
    private String id;

    private String customerId;
    private String roomId;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOuDate;
    private long nights;
    private double totalPrice;
    private BookingStatus status;//CONFIRMED, CANCELED

    public Booking() {
        // REQUIRED by Spring Data Mongo
    }

    public Booking(String customerId, Room room, LocalDate checkInDate, LocalDate checkOuDate) {
        this.customerId = customerId;
        this.roomId = room.getId();
        this.checkInDate = checkInDate;
        this.checkOuDate = checkOuDate;
        this.nights = ChronoUnit.DAYS.between(checkOuDate, checkInDate);
        this.totalPrice = calculateTotalPrice(room, nights);
        this.status = BookingStatus.CONFIRMED;
    }

    private double calculateTotalPrice(Room room, long nights) {
        return room.getPricePerNight() * nights;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getRoomId() { return roomId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOuDate; }
    public double getTotalPrice() { return totalPrice; }
    public BookingStatus getStatus() { return status; }

    public long getNights() {
        return nights;
    }

    public void setStatus(BookingStatus status) { this.status = status; }
}

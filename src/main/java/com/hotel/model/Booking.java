package com.hotel.model;

import com.hotel.model.enums.BookingStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Document(collection = "bookings")
public class Booking {
    private static long idBooking = 0;
    @Id private final long id;
    private Customer customer;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOuDate;
    private long nights;
    private double totalPrice;
    private BookingStatus status;//CONFIRMED, CANCELED

    public Booking(Customer customer, Room room, LocalDate checkInDate, LocalDate checkOuDate) {
        this.customer = customer;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOuDate = checkOuDate;
        this.nights = ChronoUnit.DAYS.between(checkOuDate, checkInDate);
        this.id = idBooking++;
        this.totalPrice = calculateTotalPrice(room, nights);
        this.status = BookingStatus.CONFIRMED;
    }

    private double calculateTotalPrice(Room room, long nights) {
        return room.getPricePerNight() * nights;
    }

    public long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Room getRoom() { return room; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOuDate; }
    public double getTotalPrice() { return totalPrice; }
    public BookingStatus getStatus() { return status; }

    public long getNights() {
        return nights;
    }

    public void setStatus(BookingStatus status) { this.status = status; }
}

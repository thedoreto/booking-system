package com.hotel.repository;

import com.hotel.model.*;
import com.hotel.model.enums.BookingStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class InMemoryHotelRepository implements HotelRepositoty {
    private ArrayList<Customer> customers = new ArrayList<>();
    private ArrayList<Room> rooms = new ArrayList<>();
    private ArrayList<Booking> bookings = new ArrayList<>();



    @Override
    public List<Booking> getActiveBookingsByCustomer(Customer customer) {
        return bookings.stream()
                .filter(b -> Objects.equals(b.getCustomer(), customer))
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .toList();
    }

    @Override
    public Optional<Customer> getCustomerByName(String name) {
        return Optional.empty();
    }
    @Override
    public Optional<Room> getRoomByNumber(int roomNumber) {
        return  rooms.stream()
                .filter(r -> roomNumber == r.getId())
                .findFirst();
    }


    @Override
    public void addCustomer(Customer customer) { customers.add(customer);  }
    @Override
    public void addRoom(Room room) { rooms.add(room); }
    @Override
    public void addBooking(Booking booking) { bookings.add(booking);  }
    @Override
    public ArrayList<Customer> getCustomers() { return customers; }
    @Override
    public ArrayList<Room> getRooms() { return rooms; }
    @Override
    public ArrayList<Booking> getBookings() { return bookings; }
}

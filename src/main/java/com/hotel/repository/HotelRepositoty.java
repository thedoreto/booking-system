package com.hotel.repository;

import com.hotel.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface HotelRepositoty {

    public ArrayList<Customer> getCustomers();
    public void addCustomer(Customer Customer);
    public ArrayList<Room> getRooms();
    public Optional<Room> getRoomByNumber(int roomNumber);
    public void addRoom(Room room);
    public ArrayList<Booking> getBookings();
    public void addBooking(Booking booking);
    public List<Booking> getActiveBookingsByCustomer(Customer customer);
    public Optional<Customer> getCustomerByName(String name) ;




}

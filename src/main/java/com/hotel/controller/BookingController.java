package com.hotel.controller;

import com.hotel.dto.BookingDTO;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.hotel.service.HotelService;
import com.hotel.repository.HotelRepositoty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
public class BookingController {

    @Autowired
    private HotelService hotelService;

    public BookingController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @PostMapping("/newBooking")
    public String createBooking(@RequestBody CreateBookingRequest request) {
        var result = hotelService.createBooking(request.checkInDate, request.checkOutDate, request.customer, request.room);
        return result.isSuccess() ? "Booking created" : "Booking failed";
    }

    @GetMapping("/booking")
    public Object getBookingByCustomer(@RequestParam String name, @RequestParam String email) {
        Customer customer = new Customer(name, email);
        return hotelService.getBookingsByCustomer(customer);
    }

    @GetMapping("/bookings")
    public List<BookingDTO> getBookingByCustomer() {
        return hotelService.getAllBookings();
    }

    public static class CreateBookingRequest {
        public LocalDate checkInDate;
        public LocalDate checkOutDate;
        public Customer customer;
        public Room room;
    }
}

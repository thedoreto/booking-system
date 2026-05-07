package com.hotel.controller;

import com.hotel.dto.BookingDTO;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.hotel.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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
  //      var result = hotelService.createBooking(request.checkInDate, request.checkOutDate, request.customer, request.room);
   //     return result.isSuccess() ? "Booking created" : "Booking failed";
        return "Create new Booking";
    }

    @GetMapping("/booking")
    public Object getBookingByCustomer(@RequestParam String name, @RequestParam String email) {
       // Customer customer = new Customer(name, email);
      //  return hotelService.getBookingsByCustomer(customer);
        return "get all bookings for a customer";
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

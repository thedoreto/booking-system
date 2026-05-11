package com.hotel.controller;

import com.hotel.dto.BookingDTO;
import com.hotel.dto.CustomerDTO;
import com.hotel.dto.RoomDTO;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.hotel.service.HotelService;
import com.hotel.service.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
public class BookingController {

    @Autowired
    private HotelService hotelService;

    public BookingController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping
    public ResponseEntity<Object> getAvailableRooms(@RequestParam String checkInDate, @RequestParam String checkOutDate) {
        LocalDate checkIn = LocalDate.parse(checkInDate);
        LocalDate checkOut = LocalDate.parse(checkOutDate);
        Result<List<RoomDTO>> result = hotelService.finaAvailableRooms(checkIn, checkOut);
        if (!result.isSuccess()) {
            return ResponseEntity
                    .badRequest()
                    .body(result.getError());
        }

        return ResponseEntity.ok(result.getData());


    }

    //get all rooms
    @GetMapping("/rooms")
    public List<RoomDTO> getAll() {
        return hotelService.getAllRooms();
    }

    //get room by id
    @GetMapping("/rooms/{id}")
    public Optional<RoomDTO> getRoom(@PathVariable String id) {
        return  hotelService.getRoomById(id);
    }

    //update existig room
    @PutMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable String id, @RequestBody RoomDTO roomDTO) {
        Optional<RoomDTO> roomOpt = hotelService.updateRoom(id, roomDTO);

        if (roomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(roomOpt.get());
    }

    //delete a room, if exists
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String id) {
        hotelService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    //create new room
    @PostMapping("/rooms/new")
    public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomDTO roomDTO) {
        Optional<RoomDTO> roomOpt = hotelService.newRoom(roomDTO);
        if (roomOpt.isEmpty())  {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(roomOpt.get());
    }

    //get all customers
    @GetMapping("customers")
    public List<CustomerDTO>  getAllCustomers() {
        return hotelService.findAllCustomers();
    }

    //get customer by id
    @GetMapping("customers/{id}")
    public Optional<CustomerDTO> getCustomer(@PathVariable String id) {
        return  hotelService.getCustomerById(id);
    }

    //update existig customer
    @PutMapping("/customers/{id}")
    public ResponseEntity<CustomerDTO> updateCustomer(@PathVariable String id, @RequestBody CustomerDTO customerDTO) {
        Optional<CustomerDTO> customerOpt = hotelService.updateCustomer(id, customerDTO);

        if (customerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(customerOpt.get());
    }

    //delete a customer, if exists
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        hotelService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    //create new customer
    @PostMapping("/customers/new")
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customerDTO) {
        Optional<CustomerDTO> customerOpt = hotelService.newCustomer(customerDTO);
        if (customerOpt.isEmpty())  {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(customerOpt.get());
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
        System.out.println("Start get bookings");
        return hotelService.getAllBookings();
    }

    public static class CreateBookingRequest {
        public LocalDate checkInDate;
        public LocalDate checkOutDate;
        public Customer customer;
        public Room room;
    }
}

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
    public ResponseEntity<List<RoomDTO>> getAll() {
        return ResponseEntity.ok(hotelService.getAllRooms());
    }

    //get room by id
    @GetMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> getRoom(@PathVariable String id) {
        Optional<RoomDTO> roomOpt = hotelService.getRoomById(id);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(roomOpt.get());
    }

    //update existig room
    @PutMapping("/rooms/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable String id, @RequestBody RoomDTO roomDTO) {
        Optional<RoomDTO> roomOpt = hotelService.updateRoom(id, roomDTO);
        return ResponseEntity.ok(roomOpt.get());
    }

    //delete a room, if exists
    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String id) {
        hotelService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    //create new room
    @PostMapping("/rooms")
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

        return ResponseEntity.ok(customerOpt.get());
    }

    //delete a customer, if exists
    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        hotelService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    //create new customer
    @PostMapping("/customers")
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customerDTO) {
        CustomerDTO customer = hotelService.newCustomer(customerDTO);
        return ResponseEntity.ok(customer);
    }
/*{
  "customerId": "...",
  "roomId": "...",
  "checkInDate": "2026-05-20",
  "checkOutDate": "2026-05-25"
}*/
    @PostMapping("/bookings")
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO) {
        BookingDTO result = hotelService.createBooking(bookingDTO);
        return ResponseEntity.ok(result);

    }


    /*PUT /bookings/{id}/cancel*/
    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable String id) {
     /*   Result<BookingDTO> result = hotelService.cancelBooking(id);
        if (!result.isSuccess()) {
            return ResponseEntity
                    .badRequest()
                    .body(null);
        }
        return ResponseEntity.ok(result.getData());) {*/
    return ResponseEntity.ok(hotelService.cancelBooking(id));

    }
    @GetMapping("/booking")
    public Object getBookingByCustomer(@RequestParam String name, @RequestParam String email) {
       // Customer customer = new Customer(name, email);
      //  return hotelService.getBookingsByCustomer(customer);
        return "get all bookings for a customer";
    }

    @GetMapping("/bookings")
    public List<BookingDTO> getAllBookings() {
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

package com.hotel.booking.controller;

import com.hotel.booking.dto.BookingDTO;
import com.hotel.booking.dto.UserDTO;
import com.hotel.booking.dto.ImageDTO;
import com.hotel.booking.dto.RoomDTO;
import com.hotel.booking.model.User;
import com.hotel.booking.model.Room;
import com.hotel.booking.service.HotelService;
import com.hotel.booking.service.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
public class BookingController {

    private final HotelService hotelService;

    public BookingController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        String email = authentication.getName();
        User user = hotelService.findUserByEmail(email);


        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "name", user.getName()
        );
    }

    @GetMapping("/rooms/available")
    public ResponseEntity<List<RoomDTO>> getAvailableRooms(@RequestParam String checkInDate, @RequestParam String checkOutDate) {
        LocalDate checkIn = LocalDate.parse(checkInDate);
        LocalDate checkOut = LocalDate.parse(checkOutDate);
        return ResponseEntity.ok(hotelService.findAvailableRooms(checkIn, checkOut));
    }

    @GetMapping("/home")
    public String start() {
        return """
        <html>
          <body>
            <p>OK - Spring is running</p>
            <a href="https://booking-ui-81fb.onrender.com/">Open app</a>
          </body>
        </html>
    """;
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

    @PostMapping("/rooms/{id}/images/add")
    public ResponseEntity<RoomDTO> addImagesToRoom(@PathVariable String id, @RequestBody List<String> imageIds) {
        Optional<RoomDTO> roomOpt = hotelService.addImagesToRoom(id, imageIds);
        if (roomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(roomOpt.get());
    }

    @DeleteMapping("/rooms/{id}/images/{imageId}")
    public ResponseEntity deleteImageFromRoom(@PathVariable("id")  String roomId, @PathVariable String imageId) {
        hotelService.deleteImageFromRoom(roomId, imageId);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/images/{id}")
    public ResponseEntity<Object> getImage(@PathVariable String id) {
        Optional<ImageDTO> imageOpt = hotelService.getImageById(id);
        if (imageOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(imageOpt.get());
    }

    @GetMapping("/images")
    public ResponseEntity<List<ImageDTO> >getAllImages() {
        return ResponseEntity.ok(hotelService.getAllImages());
    }

    @PostMapping("/images/upload")
    public ImageDTO uploadImage(@RequestBody ImageDTO image) {
        return hotelService.uploadImage(image);
    }

    @DeleteMapping("/images/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable String id) {
        hotelService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }

    //get all users
    @GetMapping("/users")
    public List<UserDTO>  getAllUsers() {
        return hotelService.findAllUsers();
    }

    //get user by id
    @GetMapping("users/{id}")
    public Optional<UserDTO> getUser(@PathVariable String id) {
        return  hotelService.getUserById(id);
    }

    //update existig user
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable String id, @RequestBody UserDTO userDTO) {
        Optional<UserDTO> userOpt = hotelService.updateUser(id, userDTO);

        return ResponseEntity.ok(userOpt.get());
    }

    //delete a user, if exists
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        hotelService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    //create new user
    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        UserDTO user = hotelService.newUser(userDTO);
        return ResponseEntity.ok(user);
    }
/*{
  "userId": "...",
  "roomId": "...",
  "checkInDate": "2026-05-20",
  "checkOutDate": "2026-05-25"
}*/
    @PostMapping("/bookings")
    public ResponseEntity<List<BookingDTO>> createBooking(@RequestBody List<BookingDTO> bookingDTOS) {
        List<BookingDTO> result = hotelService.createBooking(bookingDTOS);
        return ResponseEntity.ok(result);

    }

    /*PUT /bookings/{id}/cancel*/
    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<BookingDTO> cancelBooking(@PathVariable String id) {
        return ResponseEntity.ok(hotelService.cancelBooking(id));
    }

     @GetMapping("/bookings")
    public List<BookingDTO> getAllBookings(Authentication auth) {
        return hotelService.getAllBookings(auth);
    }

    @GetMapping("/bookings/active")
    public List<BookingDTO> getActiveBookings(Authentication auth) {
        return hotelService.getActiveBookings(auth);
    }

    @GetMapping("/bookings/future")
    public List<BookingDTO> getFutureBookings(Authentication auth) {
        return hotelService.getFutureBookings(auth);
    }

    @GetMapping("/bookings/history")
    public List<BookingDTO> getHistoryBookings(Authentication auth) {
        return hotelService.getHistoryBookings(auth);
    }

    public static class CreateBookingRequest {
        public LocalDate checkInDate;
        public LocalDate checkOutDate;
        public UserDTO user;
        public Room room;
    }
}

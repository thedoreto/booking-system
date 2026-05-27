package com.hotel.booking.service;

import com.hotel.booking.dto.BookingDTO;
import com.hotel.booking.dto.ImageDTO;
import com.hotel.booking.dto.RoomDTO;
import com.hotel.booking.dto.UserDTO;
import com.hotel.booking.model.*;
import com.hotel.booking.model.enums.BookingStatus;
import com.hotel.booking.model.enums.RoomType;
import com.hotel.common.util.ValidationUtil;
import com.hotel.booking.repository.*;
import com.hotel.common.security.UserPrincipal;
import com.hotel.booking.service.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

import com.hotel.common.util.ValidationUtil;

@Service
public class HotelService {

    private RoomMongoRepository roomRepo;
    private UserMongoRepository userRepo;
    private BookingMongoRepository bookingRepo;
    private ImageMongoRepository imageRepo;

    private Map<String, List<Booking>> bookingsByRoomId  = new HashMap<>();

    public HotelService(RoomMongoRepository roomRepo,
                        UserMongoRepository userRepo,
                        BookingMongoRepository bookingRepo,
                        ImageMongoRepository imageRepo) {
        this.roomRepo = roomRepo;
        this.userRepo = userRepo;
        this.bookingRepo = bookingRepo;
        this.imageRepo = imageRepo;
        rebuildIndex();
    }
//String checkInString, String  checkOutString, String userId, String roomId
public BookingDTO createBooking(BookingDTO bookingDTO) {

    if (bookingDTO == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking");
    }

    String userId = bookingDTO.getUserId();
    String roomId = bookingDTO.getRoomId();

    if (userId == null || roomId == null ||
            userId.isBlank() || roomId.isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid input");
    }

    LocalDate checkInDate = bookingDTO.getCheckInDate();
    LocalDate checkOutDate = bookingDTO.getCheckOutDate();

    if (!ValidationUtil.isValidPeriod(checkInDate, checkOutDate)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid dates");
    }

    if (!isRoomAvailable(roomId, checkInDate, checkOutDate)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room not available");
    }

    User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

    Room room = roomRepo.findById(roomId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room not found"));

    Booking booking = new Booking(userId, room, checkInDate.toString(), checkOutDate.toString());

    return convertBookingToDTO(bookingRepo.save(booking));
}
    public BookingDTO cancelBooking(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepo.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking not found");
        }
         Booking booking = bookingOpt.get();
        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is already canceled");
        }
        booking.setStatus(BookingStatus.CANCELED);
        bookingRepo.save(booking);
        return convertBookingToDTO(booking);
    }

    public List<BookingDTO> getBookingByUserIdAndRoomId(String userId, String roomId) {
        List<Booking> bookings = bookingRepo.findByUserIdAndRoomId(userId, roomId);
        if (bookings == null && bookings.isEmpty())    {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No bookings found for user and room");
        }
        return bookings.stream()
                .map(this::convertBookingToDTO)
                .toList();
    }

    private boolean isRoomAvailable(String roomId, LocalDate from, LocalDate to) {
        return bookingRepo
                .findByRoomIdAndStatus(roomId, BookingStatus.CONFIRMED)
                .stream()
                .noneMatch(b ->
                        isOverlap(from, to, b.getCheckInDate(), b.getCheckOutDate())
                );
    }


    private void rebuildIndex() {
        bookingsByRoomId.clear();
        for (Booking b: bookingRepo.findAll()) {
            bookingsByRoomId.computeIfAbsent(b.getRoomId(), k -> new ArrayList<>()).add(b);
        }
    }

    private boolean isOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }

    private Result<Room>  findRoom(int roomNumber) {
        if (roomNumber <= 0) {
            return Result.failure("Invalid room number");
        }
        List<Room> rooms = roomRepo.findByRoomNumber(roomNumber);
        if (rooms.isEmpty() || rooms.size() >= 1 ) {
            Result.failure("Room not found");
        }
        return Result.success(rooms.get(1));

    }

    private boolean isDateBooked(Booking b, LocalDate date) {
        return !date.isBefore(b.getCheckInDate())
                && !date.isAfter(b.getCheckOutDate());
    }

    public List<BookingDTO> getAllBookings(Authentication auth) {
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();

        if (user.getAuthorities().contains("ADMIN")) {
            return bookingRepo.findAll().stream()
                    .map(this::convertBookingToDTO)
                    .toList();
        }
        System.out.println("User id: " + user.getId());
        return bookingRepo.findByUserId(user.getId()).stream()
                .map(this::convertBookingToDTO)
                .toList();
    }

    public List<RoomDTO> getAllRooms() {
        return roomRepo.findAll().stream()
                .map(this::convertRoomToDTO)
                .toList();
    }

    public Optional<RoomDTO> getRoomById(String id) {
        Optional<Room> roomOpt = roomRepo.findById(id);
        if (roomOpt.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(convertRoomToDTO(roomOpt.get()));
    }

    public Result<List<RoomDTO>> findAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        if (!ValidationUtil.isValidPeriod(checkInDate, checkOutDate)) {
            return Result.failure("Invalid date");
        }

        List<RoomDTO> rooms = roomRepo.findAll().stream()
                .filter(room -> isRoomAvailable(room.getId(), checkInDate, checkOutDate))
                .map(this::convertRoomToDTO)
                .toList();

        return Result.success(rooms);
    }
    public List<UserDTO> findAllUsers() {
        return userRepo.findAll().stream()
                .map(this::convertUserToDTO)
                .toList();
    }

    public User findUserByEmail(String email) {
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return userOpt.get();
    }

    public Optional<UserDTO> getUserById(String id) {
        Optional<User> userOpt = userRepo.findById(id);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(convertUserToDTO(userOpt.get()));
    }

    public Optional<UserDTO> updateUser(String id, UserDTO userDTO){
        User user = userRepo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
                );

        // НЕ искам email да се променя
        if (!userDTO.getEmail().equals(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email cannot be changed");
        }

        user.setName(userDTO.getName());
        user.setRole(userDTO.getRole());
        //user.setPassword(userDTO.getPassword());
        User updated = userRepo.save(user);
        return Optional.of(convertUserToDTO(updated));

    }

    public void deleteUser(String id) {
        Optional<User> userOpt = userRepo.findById(id);
        if (userOpt.isEmpty())  {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        List<Booking> bookings = bookingRepo.findByUserId(id);
        if (bookings != null && !bookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete this user - has active bookings");
        }
        userRepo.delete(userOpt.get());
    }

    public UserDTO newUser(UserDTO userDTO) {

        if (userDTO == null
                || userDTO.getName() == null || userDTO.getName().isBlank()
                || userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Name and email are required"
            );
        }

        Optional<User> exists = userRepo.findByEmail(userDTO.getEmail());

        if (exists.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = new User(userDTO.getName(), userDTO.getEmail(), userDTO.getPassword());
        User saved = userRepo.save(user);
        return convertUserToDTO(saved);
    }


    public void deleteRoom(String id) {
        Optional<Room> roomOpt = roomRepo.findById(id);
        if (roomOpt.isEmpty())  {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
        List<Booking> bookings = bookingRepo.findByroomId(id);
        if (bookings != null && !bookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete this room - has active bookings");
        }
        roomRepo.delete(roomOpt.get());
    }

    public Optional<RoomDTO> newRoom(RoomDTO roomDTO) {
        if (roomDTO == null || roomDTO.getRoomNumber() <= 0 || roomDTO.getPricePerNight() < 0
                || !ValidationUtil.isValidRoomType(roomDTO.getType()) ) {
            return Optional.empty();
        }
        List<Room> exists = roomRepo.findByRoomNumber(roomDTO.getRoomNumber());
        if (!exists.isEmpty()) {
            return Optional.empty();
        }
        Room room = new Room(roomDTO.getRoomNumber(),
                RoomType.valueOf(roomDTO.getType()),
                roomDTO.getPricePerNight(),
                roomDTO.getImageIds());
        Room newRoom = roomRepo.save(room);
        return Optional.of(convertRoomToDTO(newRoom));
    }


    public Optional<RoomDTO> updateRoom(String id, RoomDTO roomDTO) {

        Room room = roomRepo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found")
                );

        // не искам roomNumber да се променя
        if (roomDTO.getRoomNumber() != room.getRoomNumber()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Room number cannot be changed");
        }

        room.setType(RoomType.valueOf(roomDTO.getType()));
        room.setPricePerNight(roomDTO.getPricePerNight());
      // not changing image Ids
        room.setImageIds(roomDTO.getImageIds());
        Room updated = roomRepo.save(room);
        return Optional.of(convertRoomToDTO(updated));
    }

    public Optional<RoomDTO> addImagesToRoom(String id, List<String> imageIds) {
        Optional<Room> roomOpt = roomRepo.findById(id);
        if (roomOpt.isEmpty()) {
            return Optional.empty();
        }
        Room room = roomOpt.get();
        room.getImageIds().addAll(imageIds);
        Room updated = roomRepo.save(room);
        return Optional.of(convertRoomToDTO(updated));
    }

    public void deleteImageFromRoom(String roomId, String imageId) {
        Optional<Room> roomOpt = roomRepo.findById(roomId);
        if (roomOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
        Room room = roomOpt.get();
        if (!room.getImageIds().contains(imageId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found in this room");
        }
        room.getImageIds().remove(imageId);
        roomRepo.save(room);
    }

    public List<BookingDTO> getActiveBookings(Authentication auth) {
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();
        System.out.println("User email: " + user.getEmail());

        boolean isAdmin = user.getAuthorities().contains(
                new SimpleGrantedAuthority("ADMIN")
        );
        if (isAdmin) {
            System.out.println("Admin user, fetching all active bookings");
            return bookingRepo.findByCheckInDateGreaterThanEqualAndStatus(
                            LocalDate.now(),
                            BookingStatus.CONFIRMED
                    ).stream()
                    .map(this::convertBookingToDTO)
                    .toList();
        }

        return bookingRepo.findByUserIdAndCheckInDateGreaterThanEqualAndStatus(
                        user.getId(),
                        LocalDate.now(),
                        BookingStatus.CONFIRMED)
                .stream()
                .map(this::convertBookingToDTO)
                .toList();
    }

    public List<BookingDTO> getFutureBookings(Authentication auth) {
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();
        System.out.println("User email: " + user.getEmail());

        boolean isAdmin = user.getAuthorities().contains(
                new SimpleGrantedAuthority("ADMIN")
        );
        if (isAdmin) {
            System.out.println("Admin user, fetching all future bookings");
            return bookingRepo.findByCheckInDateGreaterThanEqual(LocalDate.now())
                    .stream()
                    .map(this::convertBookingToDTO)
                    .toList();
        }

        return bookingRepo.findByUserIdAndCheckInDateGreaterThanEqual(
                        user.getId(),
                        LocalDate.now())
                .stream()
                .map(this::convertBookingToDTO)
                .toList();
    }

    public List<BookingDTO> getHistoryBookings(Authentication auth) {
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();
        System.out.println("User email: " + user.getEmail());

        boolean isAdmin = user.getAuthorities().contains(
                new SimpleGrantedAuthority("ADMIN")
        );
        if (isAdmin) {
            System.out.println("Admin user, fetching all history bookings");
            return bookingRepo.findByCheckInDateLessThan(LocalDate.now())
                    .stream()
                    .map(this::convertBookingToDTO)
                    .toList();
        }

        return bookingRepo.findByUserIdAndCheckInDateLessThan(
                        user.getId(),
                        LocalDate.now())
                .stream()
                .map(this::convertBookingToDTO)
                .toList();

    }

    public List<ImageDTO> getAllImages() {
        return imageRepo.findAll().stream()
                .map(this::convertImageToDTO)
                .toList();
    }

    public ImageDTO uploadImage(ImageDTO imageDTO) {
        if (imageDTO == null || imageDTO.getUrl() == null || imageDTO.getUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }
        Image image = new Image();
        image.setTitle(imageDTO.getTitle());
        image.setUrl(imageDTO.getUrl());
        Image saved = imageRepo.save(image);
        return convertImageToDTO(saved);
    }

    public void deleteImage(String id) {
        Optional<Image> imageOpt = imageRepo.findById(id);
        if (imageOpt.isEmpty())  {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }

        List<Room> rooms = roomRepo.findByimageIds(id);
        if (rooms != null && !rooms.isEmpty()) {
            for (Room room: rooms) {
                room.getImageIds().remove(id);
                roomRepo.save(room);
            }
        }

        imageRepo.delete(imageOpt.get());
    }

    public Optional<ImageDTO> getImageById(String id) {
        return imageRepo.findById(id).map(this::convertImageToDTO);
    }

    private ImageDTO convertImageToDTO(Image image) {
        return new ImageDTO(image.getId(), image.getUrl(), image.getTitle());
    }

    private UserDTO convertUserToDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getPassword(), user.getRole());
    }

    private BookingDTO convertBookingToDTO(Booking booking) {
        Optional<Room>  roomOpt = roomRepo.findById(booking.getRoomId());
        if (roomOpt.isEmpty())  {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Room not found for booking");
        }
        Room room = roomOpt.get();
        Optional<User> userOpt = userRepo.findById(booking.getUserId());
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User not found for booking");
        }
        User user = userOpt.get();

        return new BookingDTO(booking.getId(),
                user.getId(),
                user.getName(),
                room.getId(),
                String.valueOf(room.getRoomNumber()),
                room.getType().toString(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNights(),
                booking.getTotalPrice(),
                booking.getStatus().name());
    }

    private RoomDTO convertRoomToDTO(Room room) {
        return new RoomDTO(room.getId(),
                room.getRoomNumber(),
                room.getType().toString(),
                room.getPricePerNight(),
                room.getImageIds());
    }


}

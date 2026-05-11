package com.hotel.service;

import com.hotel.dto.BookingDTO;
import com.hotel.dto.CustomerDTO;
import com.hotel.dto.RoomDTO;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.hotel.model.enums.BookingStatus;
import com.hotel.model.enums.RoomType;
import com.hotel.model.util.ValidationUtil;
import com.hotel.repository.BookingMongoRepository;
import com.hotel.repository.CustomerMongoRepository;
import com.hotel.repository.HotelRepositoty;
import com.hotel.repository.RoomMongoRepository;
import com.hotel.service.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

@Service
public class HotelService {
 //   private HotelRepositoty repo;

    private RoomMongoRepository roomRepo;
    private CustomerMongoRepository customerRepo;
    private BookingMongoRepository bookingRepo;

    private Map<String, List<Booking>> bookingsByRoomId  = new HashMap<>();

    public HotelService(RoomMongoRepository roomRepo,
                        CustomerMongoRepository customerRepo,
                        BookingMongoRepository bookingRepo) {
        this.roomRepo = roomRepo;
        this.customerRepo = customerRepo;
        this.bookingRepo = bookingRepo;
        rebuildIndex();
    }

    public Result<Booking> createBooking(String checkInString, String  checkOutString, Customer customer, Room room) {
        if (customer == null || room == null) {
            return Result.failure("Invalid input");
        }
        LocalDate checkInDate = LocalDate.parse(checkInString);
        LocalDate checkOutDate = LocalDate.parse(checkOutString);
        if (!isValidPeriod(checkInDate, checkOutDate)) {
            return Result.failure("Invalid dates");
        }
        if (!isRoomAvailable(room, checkInDate, checkOutDate)) {
            return Result.failure("Booking not done");
        }
        Booking booking = new Booking(customer.getId(), room, checkInString, checkOutString);
        bookingsByRoomId.computeIfAbsent(room.getId(), k -> new ArrayList<>()).add(booking);
        return Result.success(booking);
    }

    public Result<Void> cancelBooking(Booking booking) {
        if (booking == null) {
            return Result.failure("Invalid booking");
        }
        booking.setStatus(BookingStatus.CANCELED);
        /*  boolean removedFromRepo = repo.getBookings().remove(booking);
        List<Booking> roomBookings = bookingsByRoomId.get(booking.getRoom().getId());
        boolean removedFromMap = roomBookings.remove(booking);
        if (!removedFromRepo && !removedFromMap) {
            return Result.failure("Booking doesn't exists");
        } */
        return Result.success();
    }

    public Result<Void> cancelBooking(String bookingId) {
        Optional<Booking> bookingOpt = bookingRepo.findById(bookingId);
     //   Optional<Booking> bookingOpt = repo.getBookings().stream()                .filter(b -> Objects.equals(b.getId(), bookingId)).findFirst();
        if (bookingOpt.isEmpty()) {
            return Result.failure("Booking doesn't exists");
        }
        return cancelBooking(bookingOpt.get());
    }

    public Result<List<Booking>> getBookingsByCustomer(Customer customer) {
        if (customer == null ) return Result.failure("Invalid Customer");
        List<Booking> bookings = bookingRepo.findByCustomerId(customer.getId());
        return Result.success(bookings);
    }

    public Result<List<Booking>> getBookingsByRoom(Room room) {
        if (room == null) return Result.failure("Room doesn't exists");
        List<Booking> bookings = bookingsByRoomId.getOrDefault(room.getId(), List.of());
        return Result.success(bookings);
    }

    public boolean isRoomAvailable(Room room, LocalDate from, LocalDate to) {
    /*     repo.getBookings().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .filter(b -> Objects.equals(b.getRoomId(), room.getId()))
                .noneMatch(b -> isOverlap(from, to, b.getCheckInDate(), b.getCheckOutDate()));*/
         return false;
    }

    private boolean isValidPeriod(LocalDate from, LocalDate to) {
        return from != null && to != null && from.isBefore(to);
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

    public List<BookingDTO> getAllBookings() {
        System.out.println("start getAllBookings");
        List<Booking> bookings = bookingRepo.findAll();
        System.out.println("start stream");
        Stream<Booking> stream = bookings.stream();
        System.out.println("start map");
         List<BookingDTO> dto=  stream.map(this::convertBookingToDTO)
                .toList();
        return dto;
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

    public Result<List<RoomDTO>> finaAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        if (!isValidPeriod(checkInDate, checkOutDate)) {
            return Result.failure("Invalid date");
        }

        List<RoomDTO> rooms = roomRepo.findAll().stream()
                .filter(room -> isRoomAvailable(room, checkInDate, checkOutDate))
                .map(this::convertRoomToDTO)
                .toList();

        return Result.success(rooms);
    }
    public List<CustomerDTO> findAllCustomers() {
        return customerRepo.findAll().stream()
                .map(this::convertCustomerToDTO)
                .toList();
    }

    public Optional<CustomerDTO> getCustomerById(String id) {
        Optional<Customer> customerOpt = customerRepo.findById(id);
        if (customerOpt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(convertCustomerToDTO(customerOpt.get()));
    }

    public Optional<CustomerDTO> updateCustomer(String id, CustomerDTO customerDTO){
        Optional<Customer> customerOpt = customerRepo.findById(id);

        if (customerOpt.isEmpty()) {
            return Optional.empty();
        }

        Customer customer = customerOpt.get();

        if (Objects.equals(customerDTO.getEmail(), customer.getEmail())) {
            return Optional.empty();
        }
        customer.setName(customerDTO.getName());

        Customer updated = customerRepo.save(customer);
        return Optional.of(convertCustomerToDTO(updated));
    }

    public void deleteCustomer(String id) {
        Optional<Customer> customerOpt = customerRepo.findById(id);
        if (customerOpt.isEmpty())  {
            return;
        }
        customerRepo.delete(customerOpt.get());
    }
    public Optional<CustomerDTO> newCustomer(CustomerDTO customerDTO) {
        if (customerDTO == null || customerDTO.getName() == null || customerDTO.getName().isEmpty()
                || customerDTO.getEmail() == null || customerDTO.getEmail().isEmpty()) {
            return Optional.empty();
        }
        List<Customer> exists = customerRepo.findByEmail(customerDTO.getEmail());
        if (!exists.isEmpty()) {
            return Optional.empty();
        }
        Customer customer = new Customer(customerDTO.getName(), customerDTO.getEmail());
        Customer newCustomer = customerRepo.save(customer);
        return Optional.of(convertCustomerToDTO(newCustomer));
     }

    private CustomerDTO convertCustomerToDTO(Customer customer) {
        return new CustomerDTO(customer.getId(), customer.getName(), customer.getEmail());
    }
    private BookingDTO convertBookingToDTO(Booking booking) {
        return new BookingDTO(booking.getId(),
                booking.getCustomerId(),
                booking.getRoomId(),
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
                room.getPricePerNight());
    }

    public void deleteRoom(String id) {
        Optional<Room> roomOpt = roomRepo.findById(id);
        if (roomOpt.isEmpty())  {
            return;
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
                roomDTO.getPricePerNight());
        Room newRoom = roomRepo.save(room);
        return Optional.of(convertRoomToDTO(newRoom));
    }


    public Optional<RoomDTO> updateRoom(String id, RoomDTO roomDTO) {
        Optional<Room> roomOpt = roomRepo.findById(id);

        if (roomOpt.isEmpty()) {
            return Optional.empty();
        }

        Room room = roomOpt.get();

        if (roomDTO.getRoomNumber() != room.getRoomNumber()) {
            return Optional.empty();
        }

        room.setType(RoomType.valueOf(roomDTO.getType()));
        room.setPricePerNight(roomDTO.getPricePerNight());

        Room updated = roomRepo.save(room);
        return Optional.of(convertRoomToDTO(updated));


    }


}

package com.hotel.service;

import com.hotel.dto.BookingDTO;
import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import com.hotel.model.enums.BookingStatus;
import com.hotel.repository.HotelRepositoty;
import com.hotel.service.result.Result;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class HotelService {
    private HotelRepositoty repo;
    private Map<Integer, List<Booking>> bookingsByRoomId  = new HashMap<>();

    public HotelService(HotelRepositoty repo) {
        this.repo = repo;
        rebuildIndex();
    }
    public Result<List<Room>> finaAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate) {
        if (!isValidPeriod(checkInDate, checkOutDate)) {
            return Result.failure("Invalid date");
        }
        List<Room> rooms = repo.getRooms().stream()
                .filter(room -> isRoomAvailable(room, checkInDate, checkOutDate))
                .toList();

        return Result.success(rooms);
    }

    public Result<Booking> createBooking(LocalDate checkInDate, LocalDate checkOutDate, Customer customer, Room room) {
        if (customer == null || room == null) {
            return Result.failure("Invalid input");
        }
        if (!isValidPeriod(checkInDate, checkOutDate)) {
            return Result.failure("Invalid dates");
        }
        if (!isRoomAvailable(room, checkInDate, checkOutDate)) {
            return Result.failure("Booking not done");
        }
        Booking booking = new Booking(customer, room, checkInDate, checkOutDate);
        bookingsByRoomId.computeIfAbsent(room.getId(), k -> new ArrayList<>()).add(booking);
        return Result.success(booking);
    }

    public Result<Void> cancelBooking(Booking booking) {
        if (booking == null) {
            return Result.failure("Invalid booking");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        /*  boolean removedFromRepo = repo.getBookings().remove(booking);
        List<Booking> roomBookings = bookingsByRoomId.get(booking.getRoom().getId());
        boolean removedFromMap = roomBookings.remove(booking);
        if (!removedFromRepo && !removedFromMap) {
            return Result.failure("Booking doesn't exists");
        } */
        return Result.success();
    }

    public Result<Void> cancelBooking(int bookingId) {
        Optional<Booking> bookingOpt = repo.getBookings().stream()
                .filter(b -> b.getId() == bookingId).findFirst();
        if (bookingOpt.isEmpty()) {
            return Result.failure("Booking doesn't exists");
        }
        return cancelBooking(bookingOpt.get());
    }

    public Result<List<Booking>> getBookingsByCustomer(Customer customer) {
        if (customer == null) return Result.failure("Invalid Customer");
        List<Booking> bookings = repo.getBookings().stream()
            .filter(b -> b.getCustomer().getId() == customer.getId()).toList();
        return Result.success(bookings);
    }

    public Result<List<Booking>> getBookingsByRoom(Room room) {
        if (room == null) return Result.failure("Room doesn't exists");
        List<Booking> bookings = bookingsByRoomId.getOrDefault(room.getId(), List.of());
        return Result.success(bookings);
    }

    public boolean isRoomAvailable(Room room, LocalDate from, LocalDate to) {
        return repo.getBookings().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .filter(b -> b.getRoom().getId() == room.getId())
                .noneMatch(b -> isOverlap(from, to, b.getCheckInDate(), b.getCheckOutDate()));
    }

    private boolean isValidPeriod(LocalDate from, LocalDate to) {
        return from != null && to != null && from.isBefore(to);
    }
    private void rebuildIndex() {
        bookingsByRoomId.clear();
        for (Booking b: repo.getBookings()) {
            bookingsByRoomId.computeIfAbsent(b.getRoom().getId(), k -> new ArrayList<>()).add(b);
        }
    }

    private boolean isOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }

    private Result<Room>  findRoom(int id) {
        if (id == 0) {
            return Result.failure("Invalid room number");
        }
        return repo.getRoomByNumber(id)
                .map(Result::success)
                .orElseGet(() -> Result.failure("Room not found"));
    }

    private boolean isDateBooked(Booking b, LocalDate date) {
        return !date.isBefore(b.getCheckInDate())
                && !date.isAfter(b.getCheckOutDate());
    }

    public List<BookingDTO> getAllBookings() {
        return repo.getBookings().stream()
                .map(b -> new BookingDTO(
                        b.getId(),
                        b.getCustomer().getId(),
                        b.getRoom().getId(),
                        b.getCheckInDate(),
                        b.getCheckOutDate(),
                        b.getNights(),
                        b.getTotalPrice(),
                        b.getStatus().name()
                )).toList();
    }
}

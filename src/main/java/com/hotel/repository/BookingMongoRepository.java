package com.hotel.repository;

import com.hotel.dto.BookingDTO;
import com.hotel.model.Booking;
import com.hotel.model.enums.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingMongoRepository
        extends MongoRepository<Booking, String> {

    List<Booking> findByCustomerIdAndStatus(
            String customerId,
            String status
    );

    List<Booking> findByCustomerIdAndCheckInDateGreaterThanEqualAndStatus(String id, LocalDate date, BookingStatus status);
    List<Booking> findByroomIdAndCheckInDateGreaterThanEqualAndStatus(String roomId, LocalDate date, BookingStatus status);

    List<Booking> findByRoomId(String roomId);

     List<Booking> findByStatus(String status);

    List<Booking> findByRoomIdAndStatus(String roomId, BookingStatus bookingStatus);

    List<Booking>  findByCustomerIdAndRoomId(String customerId, String roomId);

    List<Booking> findByCheckInDateGreaterThanEqualAndStatus(LocalDate now, BookingStatus bookingStatus);

    List<Booking> findByCheckInDateGreaterThanEqual(LocalDate now);

    List<Booking> findByCheckInDateLessThan(LocalDate now);
}
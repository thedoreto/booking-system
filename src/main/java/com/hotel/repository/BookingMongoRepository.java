package com.hotel.repository;

import com.hotel.dto.BookingDTO;
import com.hotel.model.Booking;
import com.hotel.model.enums.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Repository
public interface BookingMongoRepository
        extends MongoRepository<Booking, String> {

    List<Booking> findByUserIdAndStatus(
            String userId,
            String status
    );

    List<Booking> findByUserIdAndCheckInDateGreaterThanEqualAndStatus(String id, LocalDate date, BookingStatus status);
    List<Booking> findByRoomIdAndCheckInDateGreaterThanEqualAndStatus(String roomId, LocalDate date, BookingStatus status);

    List<Booking> findByRoomIdAndStatus(String roomId, BookingStatus bookingStatus);

    List<Booking>  findByUserIdAndRoomId(String userId, String roomId);

    List<Booking> findByCheckInDateGreaterThanEqualAndStatus(LocalDate now, BookingStatus bookingStatus);

    List<Booking> findByCheckInDateGreaterThanEqual(LocalDate now);

    List<Booking> findByCheckInDateLessThan(LocalDate now);

    List<Booking> findByUserId(String id);

    List<Booking> findByroomId(String id);

    List<Booking> findByUserIdAndCheckInDateLessThan(String id, LocalDate now);

    List<Booking>  findByUserIdAndCheckInDateGreaterThanEqual(String id, LocalDate now);
}
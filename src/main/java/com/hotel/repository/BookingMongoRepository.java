package com.hotel.repository;

import com.hotel.model.Booking;
import com.hotel.model.enums.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingMongoRepository
        extends MongoRepository<Booking, String> {

    List<Booking> findByCustomerIdAndStatus(
            String customerId,
            String status
    );

    List<Booking> findByCustomerId(String customerId);

    List<Booking> findByRoomId(String roomId);

     List<Booking> findByStatus(String status);

    List<Booking> findByRoomIdAndStatus(String roomId, BookingStatus bookingStatus);
}
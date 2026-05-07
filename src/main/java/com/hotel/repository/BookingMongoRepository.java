package com.hotel.repository;

import com.hotel.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingMongoRepository
        extends MongoRepository<Booking, String> {

    List<Booking> findByCustomerIdAndStatus(
            int customerId,
            String status
    );
}
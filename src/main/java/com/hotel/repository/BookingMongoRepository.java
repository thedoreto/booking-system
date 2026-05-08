package com.hotel.repository;

import com.hotel.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//@Repository
public interface BookingMongoRepository
        extends MongoRepository<Booking, String> {

    List<Booking> findByCustomerIdAndStatus(
            int customerId,
            String status
    );
}
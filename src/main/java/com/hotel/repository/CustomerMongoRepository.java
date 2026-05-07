package com.hotel.repository;

import com.hotel.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CustomerMongoRepository
        extends MongoRepository<Customer, String> {

    Optional<Customer> findByName(String name);
}
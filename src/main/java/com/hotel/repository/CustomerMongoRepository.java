package com.hotel.repository;

import com.hotel.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerMongoRepository
        extends MongoRepository<Customer, String> {

    Optional<Customer> findByName(String name);
}
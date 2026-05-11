package com.hotel.repository;

import com.hotel.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerMongoRepository
        extends MongoRepository<Customer, String> {


    List<Customer> findByEmail(String email);

}
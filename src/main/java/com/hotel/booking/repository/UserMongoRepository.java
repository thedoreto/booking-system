package com.hotel.booking.repository;

import com.hotel.booking.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMongoRepository
        extends MongoRepository<User, String> {


    Optional<User> findByEmail(String email);

}
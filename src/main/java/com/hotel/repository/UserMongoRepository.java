package com.hotel.repository;

import com.hotel.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserMongoRepository
        extends MongoRepository<User, String> {


    Optional<User> findByEmail(String email);

}
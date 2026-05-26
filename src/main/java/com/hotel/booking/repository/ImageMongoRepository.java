package com.hotel.booking.repository;

import com.hotel.booking.model.Image;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageMongoRepository  extends MongoRepository<Image, String> {
}

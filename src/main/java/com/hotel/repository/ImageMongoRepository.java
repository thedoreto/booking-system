package com.hotel.repository;

import com.hotel.model.Image;
import com.hotel.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageMongoRepository  extends MongoRepository<Image, String> {
}

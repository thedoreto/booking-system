package com.hotel.repository;

import com.hotel.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoomMongoRepository
        extends MongoRepository<Room, String> {

    Optional<Room> findByRoomNumber(int roomNumber);
}
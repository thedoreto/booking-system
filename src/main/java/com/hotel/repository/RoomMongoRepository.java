package com.hotel.repository;

import com.hotel.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomMongoRepository
        extends MongoRepository<Room, String> {

    Optional<Room> findByRoomNumber(int roomNumber);
}
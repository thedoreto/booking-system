package com.hotel.booking.repository;

import com.hotel.booking.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomMongoRepository
        extends MongoRepository<Room, String> {

    List<Room> findByRoomNumber(int roomNumber);

    List<Room> findByimageIds(String id);
    //  List<Booking> findByCheckInDateAndCheckOutDate(LocalDate checkInDate, LocalDate checkOutDate);
}
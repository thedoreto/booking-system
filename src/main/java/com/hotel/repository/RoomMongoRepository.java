package com.hotel.repository;

import com.hotel.model.Booking;
import com.hotel.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomMongoRepository
        extends MongoRepository<Room, String> {

    List<Room> findByRoomNumber(int roomNumber);
  //  List<Booking> findByCheckInDateAndCheckOutDate(LocalDate checkInDate, LocalDate checkOutDate);
}
package com.hotel.model;

import com.hotel.model.enums.BookingStatus;
import com.hotel.model.enums.RoomType;

public class Room {
    private static int idCount = 0;
    private final int id;
    private final int roomNumber;
    private RoomType type;//SINGLE, DOUBLE, APARTMENT
    private double pricePerNight;


    public Room(RoomType type, double pricePerNight, int roomNumber) {
        this.id = idCount++;
        this.type = type;
        this.pricePerNight = pricePerNight;
          this.roomNumber = roomNumber;
    }

    public int getId() { return id; }
    public int getRoomNumber() { return roomNumber; }
    public RoomType getType() { return type; }
    public double getPricePerNight() { return pricePerNight; }


}

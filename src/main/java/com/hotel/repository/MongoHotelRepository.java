package com.hotel.repository;

import com.hotel.model.Booking;
import com.hotel.model.Customer;
import com.hotel.model.Room;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class MongoHotelRepository implements HotelRepositoty {

    private final CustomerMongoRepository customerRepo;
    private final RoomMongoRepository roomRepo;
    private final BookingMongoRepository bookingRepo;

    public MongoHotelRepository(CustomerMongoRepository customerRepo,
                                RoomMongoRepository roomRepo,
                                BookingMongoRepository bookingRepo) {

        this.customerRepo = customerRepo;
        this.roomRepo = roomRepo;
        this.bookingRepo = bookingRepo;
    }

    @Override
    public ArrayList<Customer> getCustomers() {
        return new ArrayList<>(customerRepo.findAll());
    }

    @Override
    public void addCustomer(Customer customer) {
        customerRepo.save(customer);
    }

    @Override
    public ArrayList<Room> getRooms() {
        return new ArrayList<>(roomRepo.findAll());
    }

    @Override
    public Optional<Room> getRoomByNumber(int roomNumber) {
        return roomRepo.findByRoomNumber(roomNumber);
    }

    @Override
    public void addRoom(Room room) {
        roomRepo.save(room);
    }

    @Override
    public ArrayList<Booking> getBookings() {
        System.out.println("start in MONGO repository: number: " + bookingRepo.findAll().size());
        return new ArrayList<>(bookingRepo.findAll());
    }

    @Override
    public void addBooking(Booking booking) {
        bookingRepo.save(booking);
    }

    @Override
    public List<Booking> getActiveBookingsByCustomer(Customer customer) {

        return bookingRepo.findByCustomerIdAndStatus(
                customer.getId(),
                "CONFIRMED"
        );
    }

    @Override
    public Optional<Customer> getCustomerByName(String name) {
        return customerRepo.findByName(name);
    }
}
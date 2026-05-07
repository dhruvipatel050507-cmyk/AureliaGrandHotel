package com.aurelia.hotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aurelia.hotel.model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // 🔍 Search by type
    List<Room> findByTypeContainingIgnoreCase(String keyword);

    // 🔍 Search by room number
    List<Room> findByRoomNumberContainingIgnoreCase(String keyword);

    // 🟢 Filter by status
    List<Room> findByStatus(String status);

    // 🎯 Combined search (type + room number)
    @Query("SELECT r FROM Room r WHERE " +
           "LOWER(r.type) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Room> searchRooms(String keyword);

    // 💰 Filter by price range
    List<Room> findByPriceBetween(double min, double max);

    // 🔥 Advanced filter (status + price)
    @Query("SELECT r FROM Room r WHERE r.status = :status AND r.price BETWEEN :min AND :max")
    List<Room> filterRooms(String status, double min, double max);
}
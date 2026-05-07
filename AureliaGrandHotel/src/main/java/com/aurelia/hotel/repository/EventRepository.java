package com.aurelia.hotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aurelia.hotel.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 🔍 Search by name
    List<Event> findByNameContainingIgnoreCase(String keyword);

    // 🎯 Filter by type (Wedding, Party, etc.)
    List<Event> findByType(String type);

    // 📍 Filter by location
    List<Event> findByLocationContainingIgnoreCase(String location);

    // 🟢 Filter by status
    List<Event> findByStatus(String status);

    // 💰 Filter by price range
    List<Event> findByPriceBetween(double min, double max);

    // 🔥 Advanced search (name + description)
    @Query("SELECT e FROM Event e WHERE " +
           "LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchEvents(String keyword);

    // 📊 Count available events
    long countByStatus(String status);
}
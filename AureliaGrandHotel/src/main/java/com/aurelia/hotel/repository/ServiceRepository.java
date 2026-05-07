package com.aurelia.hotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aurelia.hotel.model.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    // 🔍 Search by name
    List<Service> findByNameContainingIgnoreCase(String keyword);

    // 🎯 Filter by category
    List<Service> findByCategory(String category);

    // 🟢 Filter by status
    List<Service> findByStatus(String status);

    // 💰 Filter by price range
    List<Service> findByPriceBetween(double min, double max);

    // 🔥 Advanced search (name + description)
    @Query("SELECT s FROM Service s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Service> searchServices(String keyword);

    // 📊 Count available services
    long countByStatus(String status);
}
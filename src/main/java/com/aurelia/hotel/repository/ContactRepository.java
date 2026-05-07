package com.aurelia.hotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aurelia.hotel.model.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    // 🔍 Search messages (name + email + message)
    @Query("SELECT c FROM Contact c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.message) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Contact> searchMessages(String keyword);

    // 🟢 Filter by status
    List<Contact> findByStatus(String status);

    // 📊 Count by status
    long countByStatus(String status);

    // 🔥 Get latest messages
    List<Contact> findTop5ByOrderByCreatedDateDesc();
}
package com.aurelia.hotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aurelia.hotel.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // 🔐 Login (email)
    User findByEmail(String email);

    // 🔐 Login with status check
    User findByEmailAndStatus(String email, String status);

    // 🔍 Search users (name OR email)
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchUsers(String keyword);

    // 🎯 Filter by role
    List<User> findByRole(String role);

    // 🟢 Filter by status
    List<User> findByStatus(String status);

    // 📊 Count users by role
    long countByRole(String role);
}
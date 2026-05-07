package com.aurelia.hotel.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aurelia.hotel.model.Booking;
import com.aurelia.hotel.model.User;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // 👤 User bookings
    List<Booking> findByUser(User user);

    // 🟢 Filter by status
    List<Booking> findByStatus(String status);

    // 💳 Filter by payment status
    List<Booking> findByPaymentStatus(String paymentStatus);

    // 📅 Bookings between dates
    List<Booking> findByCheckInBetween(LocalDate start, LocalDate end);
    List<Booking> findByRefundStatus(String refundStatus);
    
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.room.id = :roomId " +
    	       "AND b.status != 'CANCELLED' " +
    	       "AND (b.checkIn < :checkOut AND b.checkOut > :checkIn)")
    	boolean existsOverlappingBooking(Long roomId, LocalDate checkIn, LocalDate checkOut);
    // 🔥 Total Revenue (only PAID bookings)
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.paymentStatus = 'PAID'")
    Double getTotalRevenue();

    // 📊 Monthly revenue
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE MONTH(b.checkIn) = :month AND b.paymentStatus = 'PAID'")
    Double getMonthlyRevenue(int month);

    // 📈 Count approved bookings
    long countByStatus(String status);

    // 🎯 Search by room type
    @Query("SELECT b FROM Booking b WHERE LOWER(b.room.type) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Booking> searchByRoom(String keyword);
}
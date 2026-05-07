package com.aurelia.hotel.model;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private int guests;
 // 👤 Guest Details
    private String guestName;
    private String phone;
    private String email;

    // 🏨 Special Requests
    @Column(length = 1000)
    private String specialRequest;

    private double totalPrice;

    // 🆕 Total Days
    private long totalDays;

    // 🟢 Booking Status
    // PENDING / APPROVED / CANCELLED
    private String status;

    // 💳 Payment Status
    // PAID / UNPAID
    private String paymentStatus;

    // 📅 Created Date
    private LocalDate createdDate;

    // ✅ Relationship with User
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // ✅ Relationship with Room
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;
    
 // 🔁 REFUND SYSTEM
    private String refundStatus; 
    // NONE, REQUESTED, APPROVED, REJECTED, COMPLETED

    @Column(length = 500)
    private String refundReason;

    private Double refundAmount;
    
 // 🛎 Services
    @ManyToMany
    @JoinTable(
        name = "booking_services",
        joinColumns = @JoinColumn(name = "booking_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<Service> services;

    // 🎉 Events
    @ManyToMany
    @JoinTable(
        name = "booking_events",
        joinColumns = @JoinColumn(name = "booking_id"),
        inverseJoinColumns = @JoinColumn(name = "event_id")
    )
    private List<Event> events;

    // 🔥 AUTO CALCULATION METHOD
    public void calculateTotal() {
        if (checkIn != null && checkOut != null && room != null) {

            this.totalDays = ChronoUnit.DAYS.between(checkIn, checkOut);

            // minimum 1 day safety
            if (this.totalDays <= 0) {
                this.totalDays = 1;
            }

            this.totalPrice = this.totalDays * room.getPrice();
        }
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getCheckIn() {
		return checkIn;
	}

	public void setCheckIn(LocalDate checkIn) {
		this.checkIn = checkIn;
	}

	public LocalDate getCheckOut() {
		return checkOut;
	}

	public void setCheckOut(LocalDate checkOut) {
		this.checkOut = checkOut;
	}

	public double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public long getTotalDays() {
		return totalDays;
	}

	public void setTotalDays(long totalDays) {
		this.totalDays = totalDays;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public LocalDate getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public int getGuests() {
		return guests;
	}

	public void setGuests(int guests) {
		this.guests = guests;
	}

	public String getGuestName() {
		return guestName;
	}

	public void setGuestName(String guestName) {
		this.guestName = guestName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSpecialRequest() {
		return specialRequest;
	}

	public void setSpecialRequest(String specialRequest) {
		this.specialRequest = specialRequest;
	}

	public List<Service> getServices() {
		return services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public String getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(String refundStatus) {
		this.refundStatus = refundStatus;
	}

	public String getRefundReason() {
		return refundReason;
	}

	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}

	public Double getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(Double refundAmount) {
		this.refundAmount = refundAmount;
	}
}
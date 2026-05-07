package com.aurelia.hotel.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.aurelia.hotel.model.*;
import com.aurelia.hotel.repository.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired private RoomRepository roomRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private ServiceRepository serviceRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private ContactRepository contactRepo;
    @Autowired private FoodRepository foodRepo;

    // 🔐 SESSION USER
    private User getSessionUser(HttpSession session){
        return (User) session.getAttribute("loggedInUser");
    }

    // ================= HOME =================
    @GetMapping("/home")
    public String home(Model model){
        model.addAttribute("rooms", roomRepo.findByStatus("AVAILABLE"));
        model.addAttribute("services", serviceRepo.findByStatus("AVAILABLE"));
        model.addAttribute("events", eventRepo.findByStatus("AVAILABLE"));
        return "user/home";
    }

    // ================= ROOMS =================
    @GetMapping("/rooms")
    public String rooms(Model model){
        model.addAttribute("rooms", roomRepo.findByStatus("AVAILABLE"));
        return "user/rooms";
    }

    // ================= ROOM DETAILS =================
    @GetMapping("/room/{id}")
    public String roomDetails(@PathVariable Long id, Model model){
        Room room = roomRepo.findById(id).orElse(null);
        if(room == null) return "redirect:/user/rooms";

        model.addAttribute("room", room);
        return "user/room-details";
    }

    // ================= BOOK PAGE =================
    @GetMapping("/book/{id}")
    public String bookPage(@PathVariable Long id, Model model){
        model.addAttribute("room", roomRepo.findById(id).orElse(null));
        model.addAttribute("services", serviceRepo.findByStatus("AVAILABLE"));
        model.addAttribute("events", eventRepo.findByStatus("AVAILABLE"));
        return "user/book-room";
    }

    // ================= CREATE BOOKING =================
    @PostMapping("/bookRoom")
    public String bookRoom(
            @RequestParam Long roomId,
            @RequestParam String checkIn,
            @RequestParam String checkOut,
            @RequestParam int guests,

            // 👤 Guest Info
            @RequestParam String guestName,
            @RequestParam String phone,
            @RequestParam(required = false) String email,

            // 🏨 Special Request
            @RequestParam(required = false) String specialRequest,

            // 🛎 Services & Events
            @RequestParam(required = false) List<Long> services,
            @RequestParam(required = false) List<Long> events,

            HttpSession session){

        User user = getSessionUser(session);
        if(user == null) return "redirect:/login";

        Room room = roomRepo.findById(roomId).orElse(null);
        if(room == null) return "redirect:/user/rooms";

        LocalDate in = LocalDate.parse(checkIn);
        LocalDate out = LocalDate.parse(checkOut);

        // 🔐 VALIDATION
        if(out.isBefore(in) || guests <= 0){
            return "redirect:/user/book/" + roomId + "?error=invalid";
        }

        // 🚫 AVAILABILITY CHECK
        List<Booking> existing = bookingRepo.findAll();

        for(Booking b : existing){
            if(b.getRoom().getId().equals(roomId) && !b.getStatus().equals("CANCELLED")){

                if(!(out.isBefore(b.getCheckIn()) || in.isAfter(b.getCheckOut()))){
                    return "redirect:/user/book/" + roomId + "?error=unavailable";
                }
            }
        }

        Booking booking = new Booking();

        booking.setRoom(room);
        booking.setUser(user);
        booking.setCheckIn(in);
        booking.setCheckOut(out);
        booking.setGuests(guests);

        // 👤 Guest Info
        booking.setGuestName(guestName);
        booking.setPhone(phone);
        booking.setEmail(email);
        
        booking.setRefundStatus("NONE");

        // 🏨 Special Request
        booking.setSpecialRequest(specialRequest);

        // 📅 DAYS
        long days = ChronoUnit.DAYS.between(in, out);
        if(days <= 0) days = 1;

        booking.setTotalDays(days);

        // 💰 ROOM PRICE
        double total = room.getPrice() * days;

        // ➕ SERVICES
        List<Service> selectedServices = new ArrayList<>();
        if(services != null){
            for(Long sid : services){
                Service s = serviceRepo.findById(sid).orElse(null);
                if(s != null){
                    selectedServices.add(s);
                    total += s.getPrice();
                }
            }
        }
        booking.setServices(selectedServices);

        List<Event> selectedEvents = new ArrayList<>();
        if(events != null){
            for(Long eid : events){
                Event e = eventRepo.findById(eid).orElse(null);
                if(e != null){
                    selectedEvents.add(e);
                    total += e.getPrice();
                }
            }
        }
        booking.setEvents(selectedEvents);
        // 💸 TAX (10%)
        double tax = total * 0.10;
        double finalTotal = total + tax;

        booking.setTotalPrice(finalTotal);

        // 🧾 STATUS (IMPORTANT FLOW)
        booking.setPaymentStatus("UNPAID");
        booking.setStatus("PENDING");// before payment
        booking.setCreatedDate(LocalDate.now());

        bookingRepo.save(booking);

        // 🔁 GO TO PAYMENT PAGE
        return "redirect:/user/payment/" + booking.getId();
    }

    // ================= PAYMENT PAGE =================
    @GetMapping("/payment/{id}")
    public String paymentPage(@PathVariable Long id, Model model, HttpSession session){

        // 🔐 Get logged-in user
        User user = (User) session.getAttribute("loggedInUser");
        if(user == null){
            return "redirect:/login";
        }

        // 📦 Get booking
        Booking booking = bookingRepo.findById(id).orElse(null);

        // ❌ Booking not found
        if(booking == null){
            return "redirect:/user/rooms";
        }

        // 🚫 SECURITY: Check booking belongs to logged-in user
        if(!booking.getUser().getId().equals(user.getId())){
            return "redirect:/user/home";
        }

        // 🚫 Prevent reopening paid booking
        if("PAID".equalsIgnoreCase(booking.getPaymentStatus())){
            return "redirect:/user/booking?error=Already Paid";
        }

        // ✅ Add booking to view
        model.addAttribute("booking", booking);

        return "user/payment";
    }

    // ================= PAYMENT =================
    @PostMapping("/pay")
    public String pay(@RequestParam Long bookingId, HttpSession session){

        User user = getSessionUser(session);
        if(user == null) return "redirect:/login";

        Booking booking = bookingRepo.findById(bookingId).orElse(null);

        if(booking == null) return "redirect:/user/home";

        // 🔐 SECURITY CHECK
        if(!booking.getUser().getId().equals(user.getId())){
            return "redirect:/user/home";
        }

        // 🚫 ALREADY PAID
        if("PAID".equals(booking.getPaymentStatus())){
            return "redirect:/user/booking?error=Already Paid";
        }

        booking.setPaymentStatus("PAID");
        booking.setStatus("CONFIRMED");

        bookingRepo.save(booking);

        return "redirect:/user/booking?success=Payment successful!";
    }

    // ================= USER BOOKINGS =================
    @GetMapping("/booking")
    public String bookingPage(Model model, HttpSession session){

        User user = getSessionUser(session);
        if(user == null) return "redirect:/login";

        List<Booking> bookings = bookingRepo.findByUser(user);

        model.addAttribute("bookings", bookings);
        model.addAttribute("hasBookings", !bookings.isEmpty());

        return "user/booking";
    }

    // ================= CANCEL =================
    @GetMapping("/cancel-booking/{id}")
    public String cancelBooking(@PathVariable Long id){

        Booking booking = bookingRepo.findById(id).orElse(null);

        if(booking != null){
            booking.setStatus("CANCELLED");
            bookingRepo.save(booking);

            Room room = booking.getRoom();
            room.setStatus("AVAILABLE");
            roomRepo.save(room);
        }

        return "redirect:/user/booking";
    }

    // ================= CONTACT =================
    @PostMapping("/contact")
    public String contact(@RequestParam String name,
                          @RequestParam String email,
                          @RequestParam String message){

        Contact c = new Contact();
        c.setName(name);
        c.setEmail(email);
        c.setMessage(message);
        c.setStatus("NEW");

        contactRepo.save(c);

        return "redirect:/user/home";
    }

    // ================= SERVICES =================
    @GetMapping("/services")
    public String services(Model model){
        model.addAttribute("services", serviceRepo.findByStatus("AVAILABLE"));
        return "user/services";
    }

    // ================= EVENTS =================
    @GetMapping("/events")
    public String events(Model model){
        model.addAttribute("events", eventRepo.findByStatus("AVAILABLE"));
        return "user/events";
    }

    // ================= CONTACT PAGE =================
    @GetMapping("/contact")
    public String contactPage(){
        return "user/contact";
    }

    // ================= RESTAURANT =================
    @GetMapping("/restaurant")
    public String restaurant(Model model){
        model.addAttribute("foods", foodRepo.findAll());
        return "user/restaurant";
    }
    
    @PostMapping("/refund-request")
    public String requestRefund(@RequestParam Long bookingId,
                                @RequestParam String reason,
                                HttpSession session){

        User user = getSessionUser(session);
        if(user == null) return "redirect:/login";

        Booking booking = bookingRepo.findById(bookingId).orElse(null);

        if(booking == null) return "redirect:/user/booking";

        // 🔐 Security check
        if(!booking.getUser().getId().equals(user.getId())){
            return "redirect:/user/booking";
        }

        // ❌ Already requested
        if("REQUESTED".equals(booking.getRefundStatus())){
            return "redirect:/user/booking?error=Already requested";
        }

        // ❌ Check date (no refund after check-in)
        if(LocalDate.now().isAfter(booking.getCheckIn())){
            return "redirect:/user/booking?error=Too late";
        }

        booking.setRefundStatus("REQUESTED");
        booking.setRefundReason(reason);

        bookingRepo.save(booking);

        return "redirect:/user/booking?success=Refund requested";
    }
}
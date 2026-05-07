package com.aurelia.hotel.controller;

import com.aurelia.hotel.model.*;
import java.time.LocalDate;
import java.time.Duration;
import com.aurelia.hotel.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private RoomRepository roomRepo;
    @Autowired private BookingRepository bookingRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ServiceRepository serviceRepo;
    @Autowired private EventRepository eventRepo;
    @Autowired private ContactRepository contactRepo;
    @Autowired private FoodRepository foodRepo;

    

    // 🔐 ADMIN CHECK
    private boolean isAdmin(HttpSession session){
        User user = (User) session.getAttribute("loggedInUser");
        return user!=null && user.getRole().equalsIgnoreCase("ADMIN");
    }

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session){
        if(!isAdmin(session)) return "redirect:/login";

        model.addAttribute("rooms", roomRepo.count());
        model.addAttribute("bookings", bookingRepo.count());
        model.addAttribute("users", userRepo.count());

        double revenue = bookingRepo.findAll()
                .stream().mapToDouble(Booking::getTotalPrice).sum();

        model.addAttribute("revenue", revenue);
        model.addAttribute("recentBookings", bookingRepo.findAll());

        return "admin/dashboard";
    }

    // ================= ROOMS =================
    @GetMapping("/rooms")
    public String rooms(Model model, HttpSession session){
        if(!isAdmin(session)) return "redirect:/login";

        model.addAttribute("rooms", roomRepo.findAll());
        return "admin/rooms";
    }

    // ➕ ADD ROOM (WITH IMAGE)
    @PostMapping("/rooms/add")
    public String addRoom(@RequestParam("roomNumber") String roomNumber,
                          @RequestParam("type") String type,
                          @RequestParam("price") double price,
                          @RequestParam("image") MultipartFile image) throws IOException {

        // ✅ Correct upload folder (outside resources)
        String uploadDir = System.getProperty("user.dir") + "/uploads/rooms/";

        // ✅ Create folder if not exists
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // ✅ Generate unique file name
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

        // ✅ Save file
        File saveFile = new File(uploadDir + fileName);
        image.transferTo(saveFile);

        // ✅ Save data in DB
        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setType(type);
        room.setPrice(price);
        room.setStatus("AVAILABLE");
        room.setImage(fileName);

        roomRepo.save(room);

        return "redirect:/admin/rooms";
    }
    // ❌ DELETE ROOM
    @GetMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id){
        roomRepo.deleteById(id);
        return "redirect:/admin/rooms";
    }

    // 🔁 TOGGLE STATUS
    @GetMapping("/rooms/status/{id}")
    public String toggleStatus(@PathVariable Long id){
        Room r = roomRepo.findById(id).orElse(null);

        if(r != null){
            if(r.getStatus().equalsIgnoreCase("AVAILABLE")){
                r.setStatus("NOT_AVAILABLE");
            } else {
                r.setStatus("AVAILABLE");
            }
            roomRepo.save(r);
        }

        return "redirect:/admin/rooms";
    }
    
    @PostMapping("/rooms/update")
    public String updateRoom(@RequestParam("id") Long id,
                             @RequestParam("roomNumber") String roomNumber,
                             @RequestParam("type") String type,
                             @RequestParam("price") double price,
                             @RequestParam("status") String status,
                             @RequestParam("image") MultipartFile image) {

        try {
            Room room = roomRepo.findById(id).orElse(null);

            if (room != null) {

                // ✅ Image update
                if (!image.isEmpty()) {
                    String uploadDir = System.getProperty("user.dir") + "/uploads/rooms/";

                    File dir = new File(uploadDir);
                    if (!dir.exists()) dir.mkdirs();

                    String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                    image.transferTo(new File(uploadDir + fileName));

                    room.setImage(fileName);
                }

                // ✅ Update fields
                room.setRoomNumber(roomNumber);
                room.setType(type);
                room.setPrice(price);
                room.setStatus(status); // 🔥 IMPORTANT

                roomRepo.save(room);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/admin/rooms";
    }
    // ================= USERS =================
    @GetMapping("/users")
    public String users(Model model, HttpSession session){
        if(!isAdmin(session)) return "redirect:/login";

        model.addAttribute("users", userRepo.findAll());
        return "admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id){
        userRepo.deleteById(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute User user){
        userRepo.save(user);
        return "redirect:/admin/users";
    }

    // ================= BOOKINGS =================
    @GetMapping("/bookings")
    public String bookings(@RequestParam(required = false) String status,
                           @RequestParam(required = false) String keyword,
                           Model model, HttpSession session){

        if(!isAdmin(session)) return "redirect:/login";

        // 📊 Stats
        long total = bookingRepo.count();
        long pending = bookingRepo.countByStatus("PENDING");
        long approved = bookingRepo.countByStatus("APPROVED");
        long cancelled = bookingRepo.countByStatus("CANCELLED");

        Double revenue = bookingRepo.getTotalRevenue();
        if(revenue == null) revenue = 0.0;

        model.addAttribute("totalBookings", total);
        model.addAttribute("pendingBookings", pending);
        model.addAttribute("approvedBookings", approved);
        model.addAttribute("revenue", revenue);
        model.addAttribute("refundRequests", bookingRepo.findByRefundStatus("REQUESTED"));

        // 🔍 Filter logic
        java.util.List<Booking> list;

        if(status != null && !status.isEmpty()){
            list = bookingRepo.findByStatus(status);
        }
        else if(keyword != null && !keyword.isEmpty()){
            list = bookingRepo.searchByRoom(keyword);
        }
        else{
            list = bookingRepo.findAll();
        }

        model.addAttribute("bookings", list);

        return "admin/bookings";
    }
    
    // ❌ DELETE
    @GetMapping("/bookings/delete/{id}")
    public String deleteBooking(@PathVariable Long id){
        bookingRepo.deleteById(id);
        return "redirect:/admin/bookings";
    }

    // ✅ APPROVE
    @GetMapping("/bookings/approve/{id}")
    public String approveBooking(@PathVariable Long id){
        Booking b = bookingRepo.findById(id).orElse(null);
        if(b != null){
            b.setStatus("APPROVED");
            bookingRepo.save(b);
        }
        return "redirect:/admin/bookings";
    }

    // ❌ CANCEL
    @GetMapping("/bookings/cancel/{id}")
    public String cancelBooking(@PathVariable Long id){
        Booking b = bookingRepo.findById(id).orElse(null);
        if(b != null){
            b.setStatus("CANCELLED");
            bookingRepo.save(b);
        }
        return "redirect:/admin/bookings";
    }

    // ================= SERVICES =================
    @GetMapping("/services")
    public String services(Model model, HttpSession session){
        if(!isAdmin(session)) return "redirect:/login";

        model.addAttribute("services", serviceRepo.findAll());
        return "admin/services";
    }

    @PostMapping("/add-service")
    public String addService(@RequestParam("file") MultipartFile file,
                             @ModelAttribute Service service) {

        try {
            if (!file.isEmpty()) {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

                String uploadDir = System.getProperty("user.dir") + "/uploads/services/";

                java.io.File dir = new java.io.File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                file.transferTo(new java.io.File(uploadDir + fileName));

                service.setImage(fileName);
            }

            service.setStatus("AVAILABLE");
            serviceRepo.save(service);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/admin/services";
    }

    @PostMapping("/services/update")
    public String updateService(@ModelAttribute Service s,
                                @RequestParam("imageFile") MultipartFile file) {

        try {
            Service old = serviceRepo.findById(s.getId()).orElse(null);

            if (!file.isEmpty()) {

                String uploadDir = System.getProperty("user.dir") + "/uploads/services/";

                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

                file.transferTo(new File(uploadDir + fileName));

                s.setImage(fileName);

            } else if (old != null) {
                s.setImage(old.getImage());
            }

            serviceRepo.save(s);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/admin/services";
    }
    @GetMapping("/services/delete/{id}")
    public String deleteService(@PathVariable Long id){
        serviceRepo.deleteById(id);
        return "redirect:/admin/services";
    }

    // ================= EVENTS =================
    @GetMapping("/events")
    public String events(Model model, HttpSession session){
        if(!isAdmin(session)) return "redirect:/login";

        model.addAttribute("events", eventRepo.findAll());
        return "admin/events";
    }

    @PostMapping("/events/add")
    public String addEvent(@RequestParam("name") String name,
                           @RequestParam("description") String description,
                           @RequestParam("location") String location,
                           @RequestParam("price") double price,
                           @RequestParam("status") String status,
                           @RequestParam("image") MultipartFile image) throws IOException {

        // ✅ Upload folder
        String uploadDir = System.getProperty("user.dir") + "/uploads/events/";

        // ✅ Create folder if not exists
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // ✅ Save file
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        File saveFile = new File(uploadDir + fileName);
        image.transferTo(saveFile);

        // ✅ Save event
        Event e = new Event();
        e.setName(name);
        e.setDescription(description);
        e.setLocation(location);
        e.setPrice(price);
        e.setStatus(status);
        e.setImage(fileName);

        eventRepo.save(e);

        return "redirect:/admin/events";
    }

    @PostMapping("/events/update")
    public String updateEvent(@RequestParam("id") Long id,
                              @RequestParam("name") String name,
                              @RequestParam("description") String description,
                              @RequestParam("location") String location,
                              @RequestParam("price") double price,
                              @RequestParam("status") String status,
                              @RequestParam("image") MultipartFile image) throws IOException {

        Event e = eventRepo.findById(id).orElse(null);

        if (e != null) {

            // If new image uploaded
            if (!image.isEmpty()) {
                String uploadDir = System.getProperty("user.dir") + "/uploads/events/";

                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                image.transferTo(new File(uploadDir + fileName));

                e.setImage(fileName);
            }

            e.setName(name);
            e.setDescription(description);
            e.setLocation(location);
            e.setPrice(price);
            e.setStatus(status);

            eventRepo.save(e);
        }

        return "redirect:/admin/events";
    }
    @GetMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id){
        eventRepo.deleteById(id);
        return "redirect:/admin/events";
    }

    // ================= CONTACT =================
    @GetMapping("/messages")
    public String messages(Model model, HttpSession session){
        if(!isAdmin(session)) return "redirect:/login";

        model.addAttribute("contacts", contactRepo.findAll());
        return "admin/messages";
    }

    @GetMapping("/messages/delete/{id}")
    public String deleteMsg(@PathVariable Long id){
        contactRepo.deleteById(id);
        return "redirect:/admin/messages";
    }
    
    @PostMapping("/messages/reply")
    public String replyMessage(@RequestParam("email") String email,
                               @RequestParam("reply") String reply) {

        System.out.println("Reply sent to: " + email);
        System.out.println("Message: " + reply);

        return "redirect:/admin/messages";
    }
    
 // ================= FOODS =================
 // ================= FOODS =================
    @GetMapping("/foods")
    public String foods(Model model, HttpSession session){
        if(!isAdmin(session)) return "redirect:/login";

        model.addAttribute("foods", foodRepo.findAll());
        return "admin/foods";
    }


    // ================= ADD FOOD =================
    @PostMapping("/foods/add")
    public String addFood(@ModelAttribute Food food,
                          @RequestParam("imageFile") MultipartFile file) {

        try {
            if (!file.isEmpty()) {

                String uploadDir = System.getProperty("user.dir") + "/uploads/food/";

                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                file.transferTo(new File(uploadDir + fileName));

                food.setImage(fileName);
            }

            // ✅ DO NOT override status (take from form)
            foodRepo.save(food);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/admin/foods";
    }


    // ================= UPDATE FOOD =================
    @PostMapping("/foods/update")
    public String updateFood(@ModelAttribute Food food,
                             @RequestParam("imageFile") MultipartFile file) {

        try {

            Food old = foodRepo.findById(food.getId()).orElse(null);

            if (old != null) {

                // ✅ Preserve old image if new not uploaded
                if (!file.isEmpty()) {

                    String uploadDir = System.getProperty("user.dir") + "/uploads/food/";

                    File dir = new File(uploadDir);
                    if (!dir.exists()) dir.mkdirs();

                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    file.transferTo(new File(uploadDir + fileName));

                    old.setImage(fileName);

                }

                // ✅ Update fields manually (IMPORTANT)
                old.setName(food.getName());
                old.setPrice(food.getPrice());
                old.setStatus(food.getStatus());

                foodRepo.save(old);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/admin/foods";
    }


    // ================= DELETE =================
    @GetMapping("/foods/delete/{id}")
    public String deleteFood(@PathVariable Long id){
        foodRepo.deleteById(id);
        return "redirect:/admin/foods";
    }
    
    @GetMapping("/refund/approve/{id}")
    public String approveRefund(@PathVariable Long id){

        Booking b = bookingRepo.findById(id).orElse(null);

        if(b != null){

            long hours = java.time.Duration.between(
                    LocalDate.now().atStartOfDay(),
                    b.getCheckIn().atStartOfDay()
            ).toHours();

            double refund;

            if(hours >= 24){
                refund = b.getTotalPrice(); // full
            } else if(hours > 0){
                refund = b.getTotalPrice() * 0.5; // partial
            } else {
                refund = 0;
            }

            b.setRefundAmount(refund);
            b.setRefundStatus("APPROVED");
            b.setStatus("CANCELLED");

            bookingRepo.save(b);
        }

        return "redirect:/admin/bookings";
    }
    
    @GetMapping("/refund/reject/{id}")
    public String rejectRefund(@PathVariable Long id){

        Booking b = bookingRepo.findById(id).orElse(null);

        if(b != null){
            b.setRefundStatus("REJECTED");
            bookingRepo.save(b);
        }

        return "redirect:/admin/bookings";
    }
}
package com.aurelia.hotel.controller;

import com.aurelia.hotel.model.User;
import com.aurelia.hotel.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;



@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    // 🔐 PASSWORD ENCODER
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // ================= LOGIN PAGE =================
    @GetMapping("/login")
    public String loginPage(Model model) {
        return "auth/login";
    }

    // ================= SIGNUP PAGE =================
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/signup";
    }

    // ================= REGISTER =================
    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {

        // 🔍 CHECK EMAIL EXISTS
        if (userRepo.findByEmail(user.getEmail()) != null) {
            model.addAttribute("error", "Email already registered!");
            return "auth/signup";
        }

        // 🔐 ENCRYPT PASSWORD
        user.setPassword(encoder.encode(user.getPassword()));

        user.setRole("USER");

        userRepo.save(user);

        // ✅ SUCCESS MESSAGE
        model.addAttribute("success", "Account created successfully! Please login.");
        model.addAttribute("user", new User());

        return "auth/signup";
    }

    // ================= LOGIN =================
    @PostMapping("/doLogin")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          Model model,
                          HttpSession session) {

        User user = userRepo.findByEmail(email);

        // 🔐 CHECK PASSWORD (ENCRYPTED)
        if (user != null && encoder.matches(password, user.getPassword())) {

            session.setAttribute("loggedInUser", user);

            // 🔄 ROLE BASED REDIRECT
            if (user.getRole().equalsIgnoreCase("ADMIN")) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/user/home";
            }

        } else {
            model.addAttribute("error", "Invalid email or password");
            return "auth/login";
        }
    }

    // ================= LOGOUT =================
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
    
    @PostConstruct
    public void createAdmin(){
        if(userRepo.findByEmail("admin@gmail.com") == null){

            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(new BCryptPasswordEncoder().encode("123456"));
            admin.setRole("ADMIN");

            userRepo.save(admin);

            System.out.println("✅ Admin Created");
        }
    }
}
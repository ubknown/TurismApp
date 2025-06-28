package com.licentarazu.turismapp.controller;

import com.licentarazu.turismapp.model.User;
import com.licentarazu.turismapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000", "http://127.0.0.1:5173", "http://127.0.0.1:5174"}, allowCredentials = "true")
public class UserController {

    @Autowired
    private UserService userService;

    // Obține toți utilizatorii
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Înregistrare utilizator nou (cu parolă hashuită)
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    // Autentificare utilizator (verificare email + parolă)
    @PostMapping("/login")
    public String loginUser(@RequestBody User user) {
        boolean success = userService.validateUserLogin(user.getEmail(), user.getPassword());
        return success ? "Autentificare reușită" : "Email sau parolă incorectă";
    }

    // Găsește un user după email
    @GetMapping("/email/{email}")
    public Optional<User> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }
}


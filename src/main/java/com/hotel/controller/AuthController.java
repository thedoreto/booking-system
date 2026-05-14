package com.hotel.controller;

import com.hotel.repository.UserRepository;
import com.hotel.security.JwtUtil;
import com.hotel.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {

        User dbUser = userRepo.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!dbUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.status(401).body("Wrong password");
        }

        String token = jwtUtil.generateToken(dbUser.getUsername());

        return ResponseEntity.ok(Map.of("token", token));
    }
}

package com.hotel.auth;


import com.hotel.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody LoginRequest request
    ) {

        // TODO:
        // validate user from DB

        String token = jwtService.generateToken(
                "usr_123",
                request.email()
        );

        return new AuthResponse(token);
    }
}

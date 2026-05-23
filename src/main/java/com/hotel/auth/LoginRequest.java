package com.hotel.auth;

public record LoginRequest(
        String email,
        String password
) {
}

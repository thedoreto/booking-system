package com.hotel.common.security.auth;

public record LoginRequest(
        String email,
        String password
) {
}

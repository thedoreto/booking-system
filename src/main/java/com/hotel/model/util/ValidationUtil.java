package com.hotel.model.util;

import com.hotel.service.result.Result;

public class ValidationUtil {
    private ValidationUtil() {}
    public static Result<Void> requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            return Result.failure(message);
        }
        return Result.success();
    }

    public static Result<Void> requireTrue(boolean condition, String message) {
        if (!condition) {
            return Result.failure(message);
        }
        return Result.success();
    }

    public static Result<Void> isValidEmail(String email) {
        if (email == null ||
                    !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            return Result.failure("Email is not valid");
        }
        return Result.success();
    }
}

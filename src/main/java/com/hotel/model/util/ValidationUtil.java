package com.hotel.model.util;

import com.hotel.dto.RoomDTO;
import com.hotel.model.enums.RoomType;
import com.hotel.service.result.Result;

import java.util.Arrays;

public class ValidationUtil {
    private ValidationUtil() {}
    public static Result<Void> requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            return Result.failure(message);
        }
        return Result.success();
    }

    public static boolean isValidRoomType(String s) {
        return Arrays.stream(RoomType.values())
                .anyMatch(t -> t.name().equals(s));
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

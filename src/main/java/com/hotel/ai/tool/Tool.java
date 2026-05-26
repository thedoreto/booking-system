package com.hotel.ai.tool;


import org.springframework.security.core.Authentication;

public interface Tool {
    String name();
    ToolResult execute(Authentication auth);
}

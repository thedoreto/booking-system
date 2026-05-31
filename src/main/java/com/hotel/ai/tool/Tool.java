package com.hotel.ai.tool;


import com.hotel.ai.context.ToolContext;

public interface Tool {
    String name();
    ToolResult execute(ToolContext ctx);
    boolean isToolCachable();
}

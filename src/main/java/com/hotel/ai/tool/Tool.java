package com.hotel.ai.tool;


import com.hotel.ai.context.ToolContext;

/**
 * @deprecated This client is part of the legacy AI implementation.
 *             The new AI implementation uses LangChain4j.
 */
@Deprecated
public interface Tool {
    String name();
    ToolResult execute(ToolContext ctx);
    boolean isToolCachable();
}

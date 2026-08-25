package com.hotel.ai.tool;

/**
 * @deprecated This client is part of the legacy AI implementation.
 *             The new AI implementation uses LangChain4j.
 */
@Deprecated
public class ToolResult {

    private String type;
    private Object data;

    public ToolResult() {}

    public ToolResult(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public static ToolResult ok(Object data) {
        return new ToolResult("ok", data);
    }

    public static ToolResult error(String message) {
        return new ToolResult("error", message);
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}

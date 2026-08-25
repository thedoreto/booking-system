package com.hotel.ai.service;

import com.hotel.ai.tool.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @deprecated This client is part of the legacy AI implementation.
 *             The new AI implementation uses LangChain4j.
 */
@Deprecated
@Service
public class ToolRegistry {

    private final List<Tool> tools;

    public ToolRegistry(List<Tool> tools) {
        this.tools = tools;
    }

    public Tool find(String name) {
        return tools.stream()
                .filter(t -> t.name().equals(name))
                .findFirst()
                .orElse(null);
    }
}

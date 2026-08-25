package com.hotel.ai.dto;

import java.util.List;

/**
 * @deprecated Part of the legacy AI implementation.
 *             The new AI implementation uses LangChain4j.
 */
@Deprecated
public class ChatRequest {
    private String model;
    private List<Message> messages;

    public ChatRequest(String model, List<Message> messages) {
        this.model = model;
        this.messages = messages;
    }

    public List<Message> getMessages() {
        return messages;
    }
}

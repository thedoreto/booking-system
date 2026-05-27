package com.hotel.ai.dto;

public class ChatResponse {
    private Object reply;

    public ChatResponse(Object reply) {
        this.reply = reply;
    }

    public Object getReply() { return reply; }
}

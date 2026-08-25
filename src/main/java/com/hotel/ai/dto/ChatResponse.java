package com.hotel.ai.dto;
/**
 * @deprecated Part of the legacy AI implementation.
 *             The new AI implementation uses LangChain4j.
 */
@Deprecated
public class ChatResponse {
    private Object reply;

    public ChatResponse(Object reply) {
        this.reply = reply;
    }

    public Object getReply() { return reply; }
}

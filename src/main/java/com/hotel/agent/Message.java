package com.hotel.agent;

public class Message {
    private String role;
    private String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content.toLowerCase();
    }

    public String getRole() { return role; }
    public String getContent() { return content; }

    public void setContent(String content) {
        this.content = content.toLowerCase();
    }
}

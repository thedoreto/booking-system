package com.hotel.ai.controller;

import com.hotel.ai.dto.ChatRequest;
import com.hotel.ai.dto.ChatResponse;
import com.hotel.ai.service.AgentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final AgentService agentService;

    public ChatController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = agentService.handle(request.getMessages());
        return new ChatResponse(reply);
    }
}
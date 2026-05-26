package com.hotel.ai.service;

import com.hotel.ai.client.LlmClient;
import com.hotel.ai.dto.Message;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class AgentService {

    private final LlmClient llmClient;

    public AgentService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public String handle(List<Message> messages) {

        String systemPrompt =
                "Ти си помощен агент в CRM система. Отговаряш кратко.";
        messages.add(0, new Message("system", systemPrompt));
        try {
            return llmClient.ask( messages);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while asking LLM", e);
        }
    }
}
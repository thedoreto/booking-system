package com.hotel.service;

import com.hotel.agent.LlmClient;
import com.hotel.agent.Message;
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
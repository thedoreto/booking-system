package com.hotel.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.ai.client.LlmClient;
import com.hotel.ai.dto.Message;
import com.hotel.ai.tool.Tool;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class AgentService {

    private final LlmClient llmClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AgentService(LlmClient llmClient, ToolRegistry toolRegistry) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
    }

    public String handle(List<Message> messages) throws IOException {

        String systemPrompt =
                "Ти си помощен агент в CRM система. Отговаряш кратко. " +
                        "Ако трябва да извикаш tool, върни JSON: " +
                        "{type:'tool', tool:'name', input:'data'} иначе {type:'chat', content:'...' }";

        List<Message> enriched = new ArrayList<>(messages);
        enriched.add(0, new Message("system", systemPrompt));

        String response = llmClient.ask(enriched);

        JsonNode node;
        try {
            node = objectMapper.readTree(response);
        } catch (Exception e) {
            // fallback → plain chat
            return response;
        }

        String type = node.path("type").asText("chat");

        if ("tool".equals(type)) {

            String toolName = node.path("tool").asText();
            String input = node.path("input").asText();

            Tool tool = toolRegistry.find(toolName);

            if (tool == null) {
                return "Unknown tool: " + toolName;
            }

            String toolResult = tool.execute(input);

            return llmClient.askWithToolResult(enriched, toolResult);
        }

        return node.path("content").asText(response);
    }
}
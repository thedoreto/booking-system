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

    public String handle(List<Message> messages) throws Exception {

        String system = """
            You are a CRM assistant.
            Respond ONLY in JSON:

            If tool needed:
            { "type": "tool", "tool": "...", "input": "..." }

            If normal chat:
            { "type": "chat", "content": "..." }
        """;

        List<Message> input = new ArrayList<>(messages);
        input.add(0, new Message("system", system));

        String raw = llmClient.ask(input);

        JsonNode node = objectMapper.readTree(raw);
        String type = node.path("type").asText("chat");

        if (!"tool".equals(type)) {
            return node.path("content").asText(raw);
        }

        String toolName = node.path("tool").asText();
        String toolInput = node.path("input").asText();

        Tool tool = toolRegistry.find(toolName);
        if (tool == null) return "Unknown tool: " + toolName;

        String toolResult = tool.execute(toolInput);

        List<Message> withTool = new ArrayList<>(input);
        withTool.add(new Message("system", "Tool result: " + toolResult));

        String finalRaw = llmClient.ask(withTool);
        JsonNode finalNode = objectMapper.readTree(finalRaw);

        return finalNode.path("content").asText(toolResult);
    }
}
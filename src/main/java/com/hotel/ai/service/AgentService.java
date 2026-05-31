package com.hotel.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hotel.ai.client.LlmClient;
import com.hotel.ai.context.ToolContext;
import com.hotel.ai.dto.DateRange;
import com.hotel.ai.dto.Message;
import com.hotel.ai.presentation.ResponseRenderer;
import com.hotel.ai.tool.Tool;
import com.hotel.ai.tool.ToolResult;
import com.hotel.common.util.ValidationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentService {

    private final LlmClient llmClient;
    private final ToolRegistry toolRegistry;
    private final ResponseRenderer responseRenderer;

    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String system = """
            You are a CRM assistant for a hotel system.
            
            Return exactly one JSON object and nothing else.
            
            Valid outputs:
            
            1) TOOL:
            {"type":"tool","tool":"<name>","arguments":{}}
            
            2) CHAT:
            {"type":"chat","content":"<message>"}
            
            ---
            
            TOOLS:
            - get_reservations
            - get_rooms
            - get_rooms_per_dates
            
            ---
            
            RULES:
            
            If the user asks about:
            - reservations
            - rooms
            - availability
            → ALWAYS use TOOL
            
            If the user message is:
            - greeting
            - small talk
            - “what can you do”
            - “who are you”
            - unclear question
            → ALWAYS use CHAT
            
            If dates are needed for availability and missing → ask in CHAT.
            
            If unsure → CHAT.
            
            ---
            
            IMPORTANT:
            Prefer TOOL whenever the message is about hotel data (rooms, reservations, availability).
            """;

    public AgentService(LlmClient llmClient,
                        ToolRegistry toolRegistry,
                        ResponseRenderer responseRenderer) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
        this.responseRenderer = responseRenderer;
    }

    public Object handle(List<Message> messages) throws Exception {

        String userMessage = extractUserMessage(messages);
        String key = buildCacheKey(userMessage);

        Object cached = cache.get(key);
        if (cached != null) return cached;

        Object local = localRoute(userMessage);
        if (local != null) {
            cache.put(key, local);
            return local;
        }

        String raw = callLlm(userMessage);
        JsonNode node = parseLlm(raw);
        Object result = execute(node, userMessage);
        cache.put(key, result);
        return result;
    }

    private String extractUserMessage(List<Message> messages) {
        return messages.get(messages.size() - 1).getContent();
    }

    private String buildCacheKey(String userMessage) {
        return userMessage.toLowerCase().trim();
    }

    private String callLlm(String userMessage) throws Exception {

        List<Message> input = new ArrayList<>();
        input.add(new Message("system", system));
        input.add(new Message("user", userMessage));

        try {
            return llmClient.complete(input);

        } catch (HttpClientErrorException.TooManyRequests e) {
            Thread.sleep(6000 + new Random().nextInt(2000));
            return llmClient.complete(input);
        }
    }

    private JsonNode parseLlm(String raw) {

        String cleaned = raw
                .replace("```json", "")
                .replace("```", "")
                .trim();

        if (!cleaned.startsWith("{")) {
            return fallback(cleaned);
        }

        try {
            return objectMapper.readTree(cleaned);
        } catch (Exception e) {
            return fallback(cleaned);
        }
    }

    private JsonNode fallback(String text) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "chat");
        node.put("content", text);
        return node;
    }

    private Object execute(JsonNode node, String userMessage) {

        String type = node.path("type").asText("chat");
        if (!"tool".equals(type)) {
            return node.path("content").asText();
        }
        String toolName = node.path("tool").asText();
        Tool tool = toolRegistry.find(toolName);

        if (tool == null) {
            return "Unknown tool: " + toolName;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        ToolContext ctx;

        if ("get_rooms_per_dates".equals(toolName)) {

            DateRange range = ValidationUtil.extractDateRange(userMessage);

            if (range == null) {
                return ToolResult.error("MISSING_DATES");
            }

            ctx = new ToolContext(auth, userMessage, range);

        } else {
            ctx = new ToolContext(auth, userMessage);
        }

        ToolResult result = tool.execute(ctx);

        return responseRenderer.render(toolName, result);
    }

    private Object localRoute(String message) {

        String m = message.toLowerCase().trim();

        if (m.equals("hi") || m.equals("hello") || m.equals("hey") || m.equals("eho")
                    || m.equals("здравей") || m.equals("здрасти") || m.equals("привет")
                    || m.equals("ехо")) {
            return new StringBuffer("Здрасти. Мога да помагам с резервации и стаи.");
        }

        if (m.contains("какво можеш") || m.contains("what can you do")) {
            return new StringBuffer("Мога да управлявам резервации и налични стаи.");
        }

        if (m.equals("как си") || m.equals("how are you")) {
            return new StringBuffer("Готов съм да помагам с резервации и стаи.");
        }

        if (m.length() < 3) {
            return new StringBuffer("Може ли да уточниш какво имаш предвид?");
        }

        return null;
    }

  /*  private Object chat(String msg) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", "chat");
        node.put("content", msg);
        return node;
    }*/
}
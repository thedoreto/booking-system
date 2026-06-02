package com.hotel.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hotel.ai.client.LlmClient;
import com.hotel.ai.context.ToolContext;
import com.hotel.ai.dto.DateRange;
import com.hotel.ai.dto.Message;
import com.hotel.ai.presentation.ResponseRenderer;
import com.hotel.ai.tool.ReservationsTool;
import com.hotel.ai.tool.Tool;
import com.hotel.ai.tool.ToolResult;
import com.hotel.common.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final String system = """
            Return ONLY one JSON object.
            
            Format:
            {"type":"tool","tool":"<name>","arguments":{}}
            or
            {"type":"none"}
            
            Tools:
            - get_reservations
            - get_rooms
            - get_rooms_per_dates
            - strawberry_muffin
            
            SPECIAL RULE (HIGHEST PRIORITY):
                        If user mentions ANY cooking, food, or recipe → return:
            {"type":"tool","tool":"strawberry_muffin","arguments":{}}
            
            Rules:
            - get_reservations:
              ALWAYS use TOOL (no parameters required)
            - get_rooms:
              ALWAYS use TOOL (no parameters required)
            - get_rooms_per_dates:
              Use TOOL only if dates are present.
              If dates are missing → CHAT asking for dates.
              
              If no tool applies:
              Return {"type":"none"}
              DO NOT ask questions or explain anything.
            
            - Never guess missing parameters.
            """;

    private final String chatSystemPrompt = """
            You are a conversational assistant.
            
            Rules:
            - No explanations about internal processing
            - No mention of tools, models, or systems
            - Be direct and concise
            - Stay on user intent only
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

        // 1. CACHE
        Object cached = cache.get(key);
        if (cached != null) return cached;

        // 2. LOCAL ROUTE
        String local = localRoute(userMessage);
        if (local != null) {
            cache.put(key, local);
            return local;
        }

        // 3. LLM FALLBACK
        String raw = callLlm(userMessage);
        if (isOnlyChat(raw)) {
            return callLlmChat(userMessage);
        }
        JsonNode node = parseLlm(raw);

        Object result = resolveLlm(node, userMessage);
        if (shouldCache(node, result)) {
            cache.put(key, result);
        }
        return result;
    }

    private boolean isOnlyChat(String raw) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(raw);
            return "none".equals(node.path("type").asText());
        } catch (Exception e) {
            return false;
        }
    }

    private Object resolveLlm(JsonNode node, String userMessage) {

        String type = node.path("type").asText("chat");

        if (!"tool".equals(type)) {
            return node.path("content").asText();
        }

        String toolName = node.path("tool").asText();
        return executeTool(toolName, userMessage);
    }

    private boolean shouldCache(JsonNode node, Object result) {

        String type = node.path("type").asText("chat");

        // chat винаги кешираш
        if (!"tool".equals(type)) {
            return true;
        }

        String toolName = node.path("tool").asText();
        Tool tool = toolRegistry.find(toolName);

        if (tool == null) return false;
        String reqId = UUID.randomUUID().toString();
        return tool.isToolCachable();
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

    private String callLlmChat(String userMessage) throws Exception {

        List<Message> input = new ArrayList<>();
        input.add(new Message("system", chatSystemPrompt));
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

    private Object executeTool(String toolName, String userMessage) {
        log.info("Executing tool: " + toolName + " with message: " + userMessage);
        Tool tool = toolRegistry.find(toolName);
        if (tool == null) {
            return "Unknown tool: " + toolName;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        ToolContext ctx;
        if ("get_rooms_per_dates".equals(toolName)) {
            DateRange range = ValidationUtil.extractDateRange(userMessage);
            if (range == null) {
                return "Моля въведете вадни дати";
            }
            ctx = new ToolContext(auth, userMessage, range);
        } else {
            ctx = new ToolContext(auth, userMessage);
        }
        ToolResult result = tool.execute(ctx);
        return responseRenderer.render(toolName, result);
    }

    private String localRoute(String message) {

        String m = message.toLowerCase().trim();

        if (m.equals("hi") || m.equals("hello") || m.equals("hey") || m.equals("eho")
                    || m.equals("здравей") || m.equals("здрасти") || m.equals("привет")
                    || m.equals("ехо")) {
            return "Здрасти. Мога да помагам с резервации и стаи.";
        }

        if (m.contains("какво можеш") || m.contains("what can you do")) {
            return "Мога да управлявам резервации и налични стаи.";
        }

        if (m.equals("как си") || m.equals("how are you")) {
            return "Готов съм да помагам с резервации и стаи.";
        }

        if (m.length() < 3) {
            return "Може ли да уточниш какво имаш предвид?";
        }

        return null;
    }



}
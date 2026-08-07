package com.hotel.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.ai.client.GeminiClient;
import com.hotel.ai.context.ToolContext;
import com.hotel.ai.dto.DateRange;
import com.hotel.ai.dto.Message;
import com.hotel.ai.presentation.ResponseRenderer;
import com.hotel.ai.tool.Tool;
import com.hotel.ai.tool.ToolResult;
import com.hotel.common.util.ValidationUtil;
import com.hotel.knowledge.model.KnowledgeDocument;
import com.hotel.knowledge.service.KnowledgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class AgentService {

    private final GeminiClient geminiClient;
    private final KnowledgeService knowledgeService;
    private final ToolRegistry toolRegistry;
    private final ResponseRenderer responseRenderer;

    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    // Кратък и чист системен промпт само за поведението на чата
    private final String chatSystemPrompt = """
            You are a helpful and concise hotel assistant. Stay on user intent.
            """;

    public AgentService(GeminiClient geminiClient,
                        ToolRegistry toolRegistry,
                        ResponseRenderer responseRenderer,
                        KnowledgeService knowledgeService) {
        this.geminiClient = geminiClient;
        this.toolRegistry = toolRegistry;
        this.responseRenderer = responseRenderer;
        this.knowledgeService = knowledgeService;
    }

    public Object handle(List<Message> messages) throws Exception {
        String userMessage = extractUserMessage(messages);
        String key = buildCacheKey(userMessage);

        // 1. CACHE
        Object cached = cache.get(key);
        if (cached != null) return cached;

        // 2. LOCAL ROUTE (за приветствия и кратки реплики)
        String local = localRoute(userMessage);
        if (local != null) {
            cache.put(key, local);
            return local;
        }

        // 3. ПЪРВО ПРОВЕРЯВАМЕ ВЕКТОРНАТА БАЗА (RAG) - само за общи въпроси
        List<KnowledgeDocument> knowledge = knowledgeService.findRelevant(userMessage);
        if (!knowledge.isEmpty() && !isReservationOrRoomQuery(userMessage)) {
            String context = knowledge.stream()
                    .map(KnowledgeDocument::getText)
                    .collect(Collectors.joining("\n"));

            String answer = callLlmWithContext(userMessage, context);
            cache.put(key, answer);
            return answer;
        }

        // 4. ДЕКЛАРИРАМЕ ТУЛОВЕТЕ И ПИТАМЕ GEMINI С FUNCTION CALLING
        List<Map<String, Object>> registeredTools = getGeminiToolDeclarations();

        List<Message> inputMessages = new ArrayList<>();
        inputMessages.add(new Message("user", userMessage));

        String rawResponse = geminiClient.completeWithTools(inputMessages, registeredTools);
        JsonNode responseNode = objectMapper.readTree(rawResponse);

        // Проверяваме дали Gemini е решил да извика тул
        String toolName = extractFunctionCallName(responseNode);

        // Защитен механизъм: ако клиентът пита за резервации или стаи, а моделът върне текстов отговор вместо тул
        if (toolName == null) {
            String lower = userMessage.toLowerCase();
            if (lower.contains("резервац") || lower.contains("reservation")) {
                toolName = "get_reservations";
            } else if (lower.contains("стаи") || lower.contains("rooms") || lower.contains("свободни") || lower.contains("цена")) {
                toolName = "get_rooms";
            }
        }

        if (toolName != null) {
            Object result = executeTool(toolName, userMessage);

            Tool tool = toolRegistry.find(toolName);
            if (tool != null && tool.isToolCachable()) {
                cache.put(key, result);
            }
            return result;
        }

        // 5. ТЕКСТОВ ФОЛБЕК
        String textReply = extractTextReply(responseNode);
        if (textReply != null && !textReply.isBlank()) {
            cache.put(key, textReply);
            return textReply;
        }

        // 6. Краен LLM чат резервен вариант
        String fallbackAnswer = callLlmChat(userMessage);
        cache.put(key, fallbackAnswer);
        return fallbackAnswer;
    }

    private String extractUserMessage(List<Message> messages) {
        return messages.get(messages.size() - 1).getContent();
    }

    private String buildCacheKey(String userMessage) {
        return userMessage.toLowerCase().trim();
    }

    /**
     * Декларираме туловете в официалния формат на Gemini API.
     */
    private List<Map<String, Object>> getGeminiToolDeclarations() {
        return List.of(
                Map.of(
                        "name", "get_reservations",
                        "description", "Връща активните резервации на потребителя. Използвай само за лични резервации."
                ),
                Map.of(
                        "name", "get_rooms",
                        "description", "Връща списък с всички налични стаи в хотела. НЕ използвай този тул за въпроси относно общи удобства, басейн, спа, ресторанти или обща информация за хотела."
                ),
                Map.of(
                        "name", "get_rooms_per_dates",
                        "description", "Връща свободни стаи за определен период от време (дати)."
                )
        );
    }

    private String extractFunctionCallName(JsonNode root) {
        try {
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray()) {
                    for (JsonNode part : parts) {
                        if (part.has("functionCall")) {
                            return part.path("functionCall").path("name").asText();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract function call name", e);
        }
        return null;
    }

    private String extractTextReply(JsonNode root) {
        try {
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray()) {
                    for (JsonNode part : parts) {
                        if (part.has("text") && !part.path("text").asText().isBlank()) {
                            return part.path("text").asText();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract text reply", e);
        }
        return null;
    }

    /**
     * Помощен метод, който приема суров JSON стринг от Gemini и извлича от него единствено текстовия отговор.
     */
    private String parseTextFromRawJson(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            String text = extractTextReply(root);
            if (text != null) {
                return text;
            }
        } catch (Exception e) {
            log.error("Failed to parse raw JSON response from Gemini", e);
        }
        return rawJson; // fallback, ако случайно не успее да го парсира
    }

    private boolean isReservationOrRoomQuery(String message) {
        String m = message.toLowerCase();
        return m.contains("резервац") || m.contains("reservation") ||
                m.contains("стаи") || m.contains("rooms") ||
                m.contains("свободни") || m.contains("цена");
    }

    private String callLlmWithContext(String userMessage, String context)  {
        String prompt = """
            Ти си асистент на хотел.
            Отговаряй само на база предоставения контекст.
            Ако отговорът не се съдържа в контекста, кажи че нямаш информация.

            Контекст:
            %s

            Въпрос на клиента:
            %s
            """.formatted(context, userMessage);

        List<Message> messages = List.of(
                new Message("system", prompt),
                new Message("user", userMessage)
        );
        try {
            String rawResponse = geminiClient.complete(messages);
            return parseTextFromRawJson(rawResponse); // Връщаме изчистен текст, а не суров JSON!
        } catch (IOException ioe) {
            return (String) handleGeminiException(ioe, "В момента не мога да обработя заявката ви.");
        }
    }

    private String callLlmChat(String userMessage) throws Exception {
        List<Message> messages = List.of(
                new Message("system", chatSystemPrompt),
                new Message("user", userMessage)
        );
        String rawResponse = geminiClient.complete(messages);
        return parseTextFromRawJson(rawResponse); // Връщаме изчистен текст, а не суров JSON!
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
                return "Моля въведете валидни дати";
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

    private Object handleGeminiException(Exception e, String defaultMessage) {
        String msg = e.getMessage() != null ? e.getMessage() : "";
        if (msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED") || msg.contains("quota")) {
            log.warn("Gemini API quota exceeded: {}", msg);
            return "Днес изчерпихме безплатните заявки към AI асистента. Моля, опитайте отново утре!";
        }
        log.error("Gemini API error occurred", e);
        return defaultMessage;
    }
}
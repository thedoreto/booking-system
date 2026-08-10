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
import com.hotel.hotelinfo.model.KnowledgeDocument;
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
    /**
     * Списък с предварително дефинираните тулове и техните описания,
     * кеширани като константа за избягване на излишно заделяне на памет.
     */
    private static final List<Map<String, Object>> GEMINI_TOOL_DECLARATIONS = List.of(
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

    /**
     * Железният системен промпт за RAG режим — строг, отговорен и обучен да не си измисля факти,
     * а да се придържа само към предоставения хотелски контекст!
     */
    private static final String RAG_SYSTEM_PROMPT_TEMPLATE = """
        Ти си асистент на хотел.
        Отговаряй само на база предоставения контекст.
        Ако отговорът не се съдържа в контекста, кажи че нямаш информация.
        **Винаги отговаряй на същия език, на който е зададен въпросът на клиента (например ако въпросът е на английски, отговори на английски).**
        
        Контекст:
        %s

        Въпрос на клиента:
        %s
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

        // 2. LOCAL ROUTE
        Object localResult = processLocalRoute(userMessage, key);
        if (localResult != null) return localResult;

        // 3. RAG (Векторна база)
        Object ragResult = processRagKnowledge(userMessage, key);
        if (ragResult != null) return ragResult;

        // 4. TOOL CALLING & SAFETY FALLBACKS
        Object toolResult = processToolExecution(userMessage, key);
        if (toolResult != null) return toolResult;

        // 5 & 6. TEXT FALLBACK & CHAT
        return processTextFallback(userMessage, key);
    }

// ==================== ПОМОЩНИ СТЪПКИ (PIPELINE METHODS) ====================

    private Object processLocalRoute(String userMessage, String key) {
        String local = localRoute(userMessage);
        if (local != null) {
            cache.put(key, local);
            return local;
        }
        return null;
    }

    private Object processRagKnowledge(String userMessage, String key) throws Exception {
        List<KnowledgeDocument> knowledge = knowledgeService.findRelevant(userMessage);
        if (!knowledge.isEmpty() && !isReservationOrRoomQuery(userMessage)) {
            String context = knowledge.stream()
                    .map(KnowledgeDocument::getText)
                    .collect(Collectors.joining("\n"));

            String answer = callLlmWithContext(userMessage, context);
            cache.put(key, answer);
            return answer;
        }
        return null;
    }

    private Object processToolExecution(String userMessage, String key) {
        List<Map<String, Object>> registeredTools = getGeminiToolDeclarations();
        List<Message> inputMessages = List.of(new Message("user", userMessage));

        // 1. Единствената заявка към Gemini с туловете
        JsonNode responseNode;
        try {
            String rawResponse = geminiClient.completeWithTools(inputMessages, registeredTools);
            responseNode = objectMapper.readTree(rawResponse);
        } catch (IOException ioe) {
            return (String) handleGeminiException(ioe, "В момента не мога да обработя заявката ви.");
        }
        String toolName = extractFunctionCallName(responseNode);

        // Защитен механизъм за туловете (ако моделът не го е върнал сам, а го ловим по ключови думи)
        if (toolName == null) {
            String lower = userMessage.toLowerCase();
            if (lower.contains("резервац") || lower.contains("reservation")) {
                toolName = "get_reservations";
            } else if (lower.contains("стаи") || lower.contains("rooms") || lower.contains("свободни") || lower.contains("цена")) {
                toolName = "get_rooms";
            }
        }

        if (toolName != null) {
            // Изпълняваме тула
            Object toolOutput = executeToolRaw(toolName, userMessage);

            Tool tool = toolRegistry.find(toolName);
            if (tool != null && tool.isToolCachable()) {
                cache.put(key, toolOutput);
            }
            return toolOutput;
        }

        return null;
    }

    /**
     * Помощен метод, който само изпълнява тула и рендва суровия резултат без да го връща директно на клиента.
     */
    private Object executeToolRaw(String toolName, String userMessage) {
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

    private Object processTextFallback(String userMessage, String key) throws Exception {
        List<Map<String, Object>> registeredTools = getGeminiToolDeclarations();
        List<Message> inputMessages = List.of(new Message("user", userMessage));

        // Можем да си спестим повторно извикване, ако вече имаме rawResponse,
        // но тук го пазим максимално близко до оригиналната ти логика:
        String rawResponse = geminiClient.completeWithTools(inputMessages, registeredTools);
        JsonNode responseNode = objectMapper.readTree(rawResponse);

        String textReply = extractTextReply(responseNode);
        if (textReply != null && !textReply.isBlank()) {
            cache.put(key, textReply);
            return textReply;
        }

        String fallbackAnswer = callLlmChat(userMessage);
        cache.put(key, fallbackAnswer);
        return fallbackAnswer;
    }

    /**
     * Извлича последното съобщение на потребителя от чат историята.
     */
    private String extractUserMessage(List<Message> messages) {
        return messages.get(messages.size() - 1).getContent();
    }

    /**
     * Генерира стандартизиран ключ за кеширане спрямо съобщението (в малки букви и без излишни интервали).
     */
    private String buildCacheKey(String userMessage) {
        return userMessage.toLowerCase().trim();
    }

    /**
     * Декларира наличните тулове и техните описания в официалния формат на Gemini API.
     */
    private List<Map<String, Object>> getGeminiToolDeclarations() {
        return GEMINI_TOOL_DECLARATIONS;
    }

    /**
     * Обхожда JSON дървото от отговора на Gemini и извлича името на функцията/тула за изпълнение, ако има такъв.
     */
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
    /**
     * Обхожда JSON дървото от отговора на Gemini и извлича текстовия отговор, върнат от модела.
     */
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

    /**
     * Проверява дали потребителското съобщение е свързано с резервации или стаи (използва се за защитни механизми).
     */
    private boolean isReservationOrRoomQuery(String message) {
        String m = message.toLowerCase();
        return m.contains("резервац") || m.contains("reservation") ||
                m.contains("стаи") || m.contains("rooms") ||
                m.contains("свободни") || m.contains("цена");
    }

    /**
     * Извиква LLM модела, като му подава динамично намерен RAG контекст и ограничава отговорите в рамките на този контекст.
     */
    private String callLlmWithContext(String userMessage, String context)  {
        String prompt = RAG_SYSTEM_PROMPT_TEMPLATE.formatted(context, userMessage);

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

    /**
     * Извиква общия чат на LLM модела със стандартния системен промпт.
     */
    private String callLlmChat(String userMessage) throws Exception {
        List<Message> messages = List.of(
                new Message("system", chatSystemPrompt),
                new Message("user", userMessage)
        );
        String rawResponse = geminiClient.complete(messages);
        return parseTextFromRawJson(rawResponse); // Връщаме изчистен текст, а не суров JSON!
    }

    /**
     * Намира и изпълнява съответния тул (инструмент) на база името му, като подготвя необходимия контекст и валидации.
     */
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

    /**
     * Локален рутер за бързо обработване на приветствия, кратки реплики и общи въпроси без нужда от заявки към LLM.
     */
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

    /**
     * Обработва изключения при заявки към Gemini API, като прави проверка за изчерпани лимити (код 429)
     * и връща подходящо съобщение към потребителя, или логва грешката при друг тип проблем.
     */
    private Object handleGeminiException(Exception e, String defaultMessage) {
        String msg = e.getMessage() != null ? e.getMessage() : "";
        if (msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED") || msg.contains("quota")) {
            log.warn("Gemini API quota exceeded: {}", msg);
            return "Изчерпахте безплатните заявки към AI асистента. Моля, опитайте отново по-късно!";
        }
        log.error("Gemini API error occurred", e);
        return defaultMessage;
    }
}
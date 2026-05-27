package com.hotel.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.ai.client.LlmClient;
import com.hotel.ai.context.ToolContext;
import com.hotel.ai.dto.DateRange;
import com.hotel.ai.dto.Message;
import com.hotel.ai.tool.Tool;
import com.hotel.ai.tool.ToolResult;
import com.hotel.common.util.ValidationUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentService {

    private final LlmClient llmClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String system = """
            You are a STRICT CRM ROUTING ENGINE.
            
            You do NOT chat.
            You do NOT entertain.
            You do NOT answer general questions.
            
            Your ONLY job is to choose a tool or refuse.
            
            ---
            
            AVAILABLE TOOLS (ONLY VALID OPTIONS):
                Имаш достъп до следните tools:
                
                1. get_reservations
                - връща всички резервации
                
                2. get_rooms
                - връща всички стаи
                
                3. get_rooms_per_dates
                - намира свободни стаи за даден период
                - arguments:
                    - checkin (format: yyyy-MM-dd)
                    - checkout (format: yyyy-MM-dd)
                
                Когато потребителят иска свободни стаи за период,
                трябва да извикаш:
                
                {
                  "type": "tool",
                  "tool": "get_rooms_per_dates",
                  "arguments": {
                      "checkin": "2026-06-01",
                      "checkout": "2026-06-05"
                  }
                }
                
                Не измисляй дати.
                Ако липсват дати — поискай ги от потребителя.
            
            ---
            
            RULES (ABSOLUTE):
            
            1. If user request can be mapped to a tool:
               Respond ONLY with:
            
               { "type": "tool", "tool": "<exact_tool_name>", "input": "<string_or_empty>" }
            
            2. If user request is NOT clearly related to available tools:
               Respond ONLY with:
            
               { "type": "refusal" }
            
               No explanation.
               No text.
               No conversation.
            
            3. NEVER:
               - invent tools
               - chat
               - add extra fields
               - return markdown
               - return natural language
            
            4. INPUT HANDLING:
               - "input" must be string or empty string ""
               - do NOT return objects or nested JSON
            
            ---
            
            EXAMPLES:
            
            User: "резервации"
            → { "type": "tool", "tool": "get_reservations", "input": "" }
            
            User: "свободни стаи"
            → { "type": "tool", "tool": "get_rooms", "input": "" }
            
            User: "ехооо"
            → { "type": "refusal" }
            
            User: "как си"
            → { "type": "refusal" }
            
            ---
            
            REMEMBER:
            You are NOT a chatbot.
            You are a command router only.
            """;

    public AgentService(LlmClient llmClient, ToolRegistry toolRegistry) {
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
    }

    public Object handle(List<Message> messages) throws Exception {

        List<Message> input = new ArrayList<>(messages);
        input.add(0, new Message("system", system));

        String raw = llmClient.complete(input);

        JsonNode node = objectMapper.readTree(raw);
        String type = node.path("type").asText("chat");

        if (!"tool".equals(type)) {
            return node.path("content").asText(raw);
        }

        String toolName = node.path("tool").asText();
        Tool tool = toolRegistry.find(toolName);

        if (tool == null) {
            return "Unknown tool: " + toolName;
        }

        String userMessage = messages.get(messages.size() - 1).getContent();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        ToolContext ctx;

        // ONLY special-case parameter enrichment
        if (toolName.equals("get_rooms_per_dates")) {
            DateRange range = ValidationUtil.extractDateRange(userMessage);
            if (range == null) {
                return ToolResult.error("MISSING_DATES");
            }

            ctx = new ToolContext(auth, userMessage, range);
        } else {
            ctx = new ToolContext(auth, userMessage);
        }

        return tool.execute(ctx);
    }


}
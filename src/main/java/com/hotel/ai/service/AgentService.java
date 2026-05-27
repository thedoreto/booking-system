package com.hotel.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentService {

    private final LlmClient llmClient;
    private final ToolRegistry toolRegistry;
    private final ResponseRenderer responseRenderer;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String system = """
            You are a CRM routing engine with limited conversational ability.
            
            Your primary job is to route requests to tools.
            However, if no tool clearly matches the request, you MAY respond briefly in natural language.
            
            ---
            
            AVAILABLE TOOLS:
            
            1. get_reservations
            - returns all reservations
            
            2. get_rooms
            - returns all rooms
            
            3. get_rooms_per_dates
            - finds available rooms for a date range
            - arguments:
                - checkin (yyyy-MM-dd)
                - checkout (yyyy-MM-dd)
            
            ---
            
            TOOL SELECTION RULES:
            
            If the user request clearly matches a tool → call tool.
            
            If dates are needed:
            - try to extract them from natural language
            - support formats like:
              - "next Friday"
              - "from June 1 to June 5"
              - "01.06 - 05.06"
              - "between 1st and 5th June"
            
            If dates are missing or ambiguous:
            → ask a short clarifying question (NOT refusal)
            
            ---
            
            OUTPUT FORMAT (STRICT FOR TOOLS ONLY):
            
            {
              "type": "tool",
              "tool": "<tool_name>",
              "arguments": {
                ...
              }
            }
            
            If no tool applies AND no clear intent:
            → respond with:
            
            {
              "type": "chat",
              "content": "<short helpful response>"
            }
            
            If user input is invalid or nonsense:
            →
            
            {
              "type": "chat",
              "content": "Може ли да уточниш какво имаш предвид?"
            }
            
            ---
            
            STRICT RULES:
            
            - Never invent tools
            - Never output extra fields in tool mode
            - Never return markdown
            - Keep responses minimal
            - Prefer tool usage when uncertain
            
            ---
            
            EXAMPLES:
            
            User: "свободни стаи за другия уикенд"
            → tool(get_rooms_per_dates)
            
            User: "резервации"
            → tool(get_reservations)
            
            User: "как си"
            → chat("Мога да помагам с резервации и стаи.")
            
            User: "from 1 June to 5 June"
            → tool(get_rooms_per_dates)
            
            ---
            """;

    public AgentService(LlmClient llmClient, ToolRegistry toolRegistry, ResponseRenderer responseRenderer) {
        this.responseRenderer = responseRenderer;
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

        Object result = tool.execute(ctx);

        return responseRenderer.render(toolName, result);
    }


}
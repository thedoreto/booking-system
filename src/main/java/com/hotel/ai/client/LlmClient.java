package com.hotel.ai.client;

import com.hotel.ai.dto.Message;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmClient {

    private String model = "llama-3.1-8b-instant";

    @Value("${llm.api.key}")
    private String apiKey;

    private String url = "https://api.groq.com/openai/v1/chat/completions";
    private String contentType = "application/json";
    private OkHttpClient client;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LlmClient(OkHttpClient client) {
        this.client = client;
    }

    public String ask(List<Message> messages) throws IOException {
        String content =  messages.get(messages.size() - 1).getContent().toLowerCase();
        System.out.println("Received user message: " + content);

        // 1. ако има ключова дума за инструмент → CALL_TOOL
        if (content.contains("резервация") || content.contains("резервации")) {
            return "CALL_TOOL_GET_RESERVATIONS";
        }

        if (content.contains("стая") || content.contains("стаи") || content.contains("стаята")) {
            return "CALL_TOOL_GET_ROOMS";
        }

        if (content.contains("парола") || content.contains("пароли") || content.contains("паролата")) {
            return "Нямам право да давам тази информация.";
        }

        // 3. ако няма правила → LLM
         String requestJson = buildRequest(messages);
         return callLLM(requestJson);
    }

    public String askWithToolResult(List<Message> messages, String toolResult) throws IOException {
        messages.add(new Message("system", "Tool result: " + toolResult));
        return ask(messages);
    }

//    record RequestBody(String model, List<Message> messages) {}

    private String callLLM(String message) throws IOException {

        // reuse the injected client if available
        OkHttpClient httpClient = this.client != null ? this.client : new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", contentType)
                .post(RequestBody.create(message, MediaType.parse(contentType)))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody respBody = response.body();
            return extractReply(respBody.string());
        } catch (Exception e) {
            throw new IOException("Error occurred while calling LLM API", e);
        }
    }

    private String buildRequest(List<Message> messages) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        try {
            return objectMapper.writeValueAsString(body);
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize request body", e);
        }
    }

    private String extractReply(String json) throws IOException {

        return objectMapper.readTree(json)
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
    }


}


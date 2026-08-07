package com.hotel.ai.client;

import com.hotel.ai.dto.Message;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiClient {

    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String apiKey;
    private final String url;


    public GeminiClient(@Value("${gemini.api.key}") String apiKey,
                        @Value("${gemini.api.base-url}") String baseUrl,
                        @Value("${gemini.base.model}") String model,
                        OkHttpClient client) {
        this.apiKey = apiKey;
        this.url = baseUrl + "/" + model + ":generateContent?key=";
        this.client = client;
    }

    public String complete(List<Message> messages) throws IOException {
        // Временно връщаме готов JSON за тестове (разкоментирай при нужда)
       //  return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Тестов отговор от локален мок\"}]}}]}";
        return completeWithTools(messages, null);
    }

    public String completeWithTools(List<Message> messages, List<Map<String, Object>> tools) throws IOException {
        String body = buildRequest(messages, tools);

        Request request = new Request.Builder()
                .url(url + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("Empty response body from Gemini API");
            }
            String json = responseBody.string();
            if (!response.isSuccessful()) {
                throw new IOException("Gemini API error: " + response.code() + " - " + json);
            }
            return json;
        }
    }

    private String buildRequest(List<Message> messages, List<Map<String, Object>> tools) throws IOException {
        Map<String, Object> root = new HashMap<>();

        // 1. Извличаме системното съобщение (ако има такова) и го слагаме където го иска Gemini
        List<Message> chatMessages = new ArrayList<>();
        for (Message msg : messages) {
            if ("system".equalsIgnoreCase(msg.getRole())) {
                root.put("systemInstruction", Map.of("parts", List.of(Map.of("text", msg.getContent()))));
            } else {
                chatMessages.add(msg);
            }
        }

        // 2. Преобразуваме останалите съобщения в Gemini формат (contents -> parts)
        List<Map<String, Object>> contents = chatMessages.stream().map(msg -> {
            Map<String, Object> part = Map.of("text", msg.getContent());

            // Gemini разпознава само "user" и "model"
            String role = "assistant".equalsIgnoreCase(msg.getRole()) ? "model" : "user";

            Map<String, Object> contentMap = new HashMap<>();
            contentMap.put("role", role);
            contentMap.put("parts", List.of(part));
            return contentMap;
        }).collect(Collectors.toList());

        root.put("contents", contents);

        // 3. Добавяме туловете, ако има такива
        if (tools != null && !tools.isEmpty()) {
            root.put("tools", List.of(Map.of("functionDeclarations", tools)));
        }

        return objectMapper.writeValueAsString(root);
    }

    public String getUrl() {
        return url + apiKey;
    }
}
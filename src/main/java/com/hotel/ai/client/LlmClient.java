package com.hotel.ai.client;

import com.hotel.ai.dto.Message;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmClient {

    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${llm.api.key}")
    private String apiKey;

    private final String url = "https://api.groq.com/openai/v1/chat/completions";
    private final String model = "llama-3.1-8b-instant";

    public LlmClient(OkHttpClient client) {
        this.client = client;
    }

    public String complete(List<Message> messages) throws IOException {

        String body = buildRequest(messages);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            System.out.println("LLM API response code: " + responseBody);
            if (responseBody == null) {
                throw new IOException("Empty response body from LLM API");
            }
            String json = responseBody.string();
            if (!response.isSuccessful()) {
                throw new IOException("API error: " + response.code() + " - " + json);
            }
            return extractReply(json);
        }
    }

    private String buildRequest(List<Message> messages) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        return objectMapper.writeValueAsString(body);
    }

    private String extractReply(String json) throws IOException {
        try {
            JsonNode root = objectMapper.readTree(json);

            // Check if response has the expected structure
            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                JsonNode choice = root.get("choices").get(0);
                if (choice.has("message") && choice.get("message").has("content")) {
                    return choice.get("message").get("content").asText();
                }
            }

            // Fallback: if structure is unexpected, return the whole response
            throw new IOException("Unexpected API response structure: " + json);
        } catch (Exception e) {
            throw new IOException("Failed to parse LLM response: " + e.getMessage(), e);
        }
    }
}


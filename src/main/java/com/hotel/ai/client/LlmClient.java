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
            String json = response.body().string();
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
        return objectMapper.readTree(json)
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
    }
}


package com.hotel.ai.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeminiEmbeddingClient  {
    private final String apiKey;
    private final String url;


    public GeminiEmbeddingClient(@Value("${gemini.api.key}") String apiKey,
                        @Value("${gemini.api.base-url}") String baseUrl,
                        @Value("${gemini.embedding.model}") String model) {
        this.apiKey = apiKey;
        this.url = baseUrl + "/" + model + ":embedContent?key=";
    }

    /**
     * Генерира ембединг с указан taskType (напр. "RETRIEVAL_DOCUMENT" или "RETRIEVAL_QUERY").
     */
    public List<Double> getEmbedding(String text, String taskType) throws Exception {
        // Почистваме текста от кавички и нови редове, за да не счупим JSON-а
        String sanitizedText = text.replace("\"", "\\\"").replace("\n", " ");

        // Създаваме JSON заявката, включвайки taskType според изискванията на Gemini API
        String requestBody = String.format(
                "{\"content\": {\"parts\": [{\"text\": \"%s\"}]}, \"taskType\": \"%s\"}",
                sanitizedText, taskType
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url + apiKey))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Грешка от Gemini API: " + response.body());
        }

        // Извличаме числата от отговора с бърз Regex (без нужда от тежки библиотеки за парсиране)
        return extractValuesWithRegex(response.body());
    }

    /**
     * Помощен метод по подразбиране (третира текса като потребителска заявка за търсене).
     */
    public List<Double> getEmbedding(String text) throws Exception {
        return getEmbedding(text, "RETRIEVAL_QUERY");
    }

    private static List<Double> extractValuesWithRegex(String jsonResponse) {
        List<Double> embedding = new ArrayList<>();
        // Търси секцията "values": [ 0.123, -0.456, ... ] в отговора
        Pattern pattern = Pattern.compile("\"values\":\\s*\\[(.*?)\\]", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(jsonResponse);

        if (matcher.find()) {
            String numbersGroup = matcher.group(1);
            String[] numbers = numbersGroup.split(",");
            for (String num : numbers) {
                embedding.add(Double.parseDouble(num.trim()));
            }
        }
        return embedding;
    }
}

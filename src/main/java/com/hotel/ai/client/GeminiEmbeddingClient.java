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
    @Value("${gemini.api.key}")
    private String API_KEY;

    private static final String MODEL = "gemini-embedding-001";
    private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":embedContent?key=";

    public List<Double> getEmbedding(String text) throws Exception {
        // Почистваме текста от кавички и нови редове, за да не счупим JSON-а
        String sanitizedText = text.replace("\"", "\\\"").replace("\n", " ");

        // Създаваме JSON съвсем ръчно като обикновен текст
        String requestBody = String.format("{\"content\":{\"parts\":[{\"text\":\"%s\"}]}}", sanitizedText);

        HttpClient client = HttpClient.newHttpClient();
        System.out.println("Api key: " + API_KEY);
        System.out.println("Api key: " + API_KEY);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL + API_KEY))
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

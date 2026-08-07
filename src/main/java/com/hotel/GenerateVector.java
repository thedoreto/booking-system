package com.hotel;


import com.hotel.ai.client.GeminiEmbeddingClient;

import java.util.List;
import java.util.stream.Collectors;

public class GenerateVector {

    public static void main(String[] args) {
        try {
            GeminiEmbeddingClient geminiService = new GeminiEmbeddingClient();
            List<Double> em = geminiService.getEmbedding("Закуската се сервира между 7:30 и 10:00.");
            String vectorString = em.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));

            // Принтираме готовия низ в конзолата
            System.out.println("[" + vectorString + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

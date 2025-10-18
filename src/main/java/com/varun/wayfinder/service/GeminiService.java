package com.varun.wayfinder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String generateTravelRoute(String placeName, String country, String description, int days) {
        String prompt = String.format(
                "Create a detailed %d-day travel itinerary for %s, %s. " +
                        "Description: %s\n\n" +
                        "Format the response as a day-by-day itinerary with:\n" +
                        "- Morning activities\n" +
                        "- Afternoon activities\n" +
                        "- Evening activities\n" +
                        "- Restaurant recommendations\n" +
                        "- Transportation tips\n" +
                        "Make it engaging, practical, and include estimated costs in USD.",
                days, placeName, country, description
        );

        try {
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();

            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            String response = webClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseGeminiResponse(response);

        } catch (Exception e) {
            e.printStackTrace();
            return "Error generating itinerary. Please try again later.";
        }
    }

    private String parseGeminiResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }

            return "Unable to generate itinerary at this time.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing response.";
        }
    }
}
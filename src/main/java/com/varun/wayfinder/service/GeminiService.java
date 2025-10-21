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
                "You are a professional travel planner. Create a detailed, engaging, and realistic %d-day travel itinerary for %s, %s.\n\n" +
                        "Traveler's preferences: %s\n\n" +
                        "Your response should be structured clearly with markdown headings for each day (e.g., 'Day 1: ...').\n" +
                        "For each day, include the following sections:\n" +
                        "1. Morning activities – sightseeing, cultural experiences, or nature spots\n" +
                        "2. Afternoon activities – local attractions, tours, or relaxation options\n" +
                        "3. Evening activities – nightlife, events, or scenic spots\n" +
                        "4. Restaurant recommendations – include 2-3 local dining options with cuisine type and estimated cost per person (in USD)\n" +
                        "5. Transportation tips – best ways to get around (walking, taxi, metro, etc.) and approximate daily cost\n\n" +
                        "Additional requirements:\n" +
                        "- Focus on a realistic and enjoyable pace (not too rushed)\n" +
                        "- Highlight unique local experiences or hidden gems\n" +
                        "- Include short tips or notes where relevant (e.g., best time to visit, ticket info)\n" +
                        "- Keep costs and details practical for budget or mid-range travelers\n\n" +
                        "Return the response in well-formatted markdown, ready to display directly to users.",
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
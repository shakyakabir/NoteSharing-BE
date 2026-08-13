package com.example.notesharing.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class ImageGenerationService {
    private final RestClient restClient;

    public ImageGenerationService(
            RestClient.Builder builder,
            @Value("${unsplash.access-key}") String accessKey
    ) {
        this.restClient = builder
                .baseUrl("https://api.unsplash.com")
                .defaultHeader("Authorization", "Client-ID " + accessKey)
                .build();
    }

    @SuppressWarnings("unchecked")
    public String generateImage(String prompt) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search/photos")
                            .queryParam("query", prompt)
                            .queryParam("per_page", 1)
                            .queryParam("orientation", "landscape")
                            .build())
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results != null && !results.isEmpty()) {
                Map<String, String> urls = (Map<String, String>) results.get(0).get("urls");
                return urls.get("regular");
            }
        } catch (Exception e) {
            System.err.println("Image search failed for [" + prompt + "]: " + e.getMessage());
        }
        return null;
    }
}

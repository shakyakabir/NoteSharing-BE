package com.example.notesharing.DTO.Response.Presentation;

import java.util.Map;

public record VisualElement(String type,
                            String title,
                            String content,
                            String position,
                            Map<String, Object> data) {
}

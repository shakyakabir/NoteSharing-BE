package com.example.notesharing.DTO.Response.Presentation;

public record ImageSpec(boolean required,
                        String prompt,
                        String position,
                        double width,
                        double height) {
}

package com.example.notesharing.DTO.Response.Presentation;



import java.util.List;

public record SlideContent(String title, String subTitle, String content, List<String> bullets, ImageSpec image, String slideType, Layout layout, List<VisualElement> visualElements) {}
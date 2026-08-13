package com.example.notesharing.DTO.Response.Presentation;


import java.util.List;

public record PresentationContent(String title, List<SlideContent> slides) {}


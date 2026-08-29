package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdvertisementRequest {

    private String title;
    private String description;
    private String imageUrl;
    private String targetUrl;
    private String placement;
    private double cpmRate;
    private double cpcRate;
    private String status;
}

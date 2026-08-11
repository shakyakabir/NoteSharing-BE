package com.example.notesharing.DTO.Request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PointTransactionRequest {

    private String userEmail;
    private int amount;
    private String description;
}

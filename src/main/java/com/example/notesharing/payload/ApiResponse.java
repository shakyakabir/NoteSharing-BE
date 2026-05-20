package com.example.notesharing.payload;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private String message;
    private String status;
    private T data;

}

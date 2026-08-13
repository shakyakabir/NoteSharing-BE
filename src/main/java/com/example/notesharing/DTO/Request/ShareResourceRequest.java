package com.example.notesharing.DTO.Request;

import com.example.notesharing.Enum.SharedResourceType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ShareResourceRequest {

    private String sharedByEmail;
    private SharedResourceType resourceType;
    private UUID resourceId;
}

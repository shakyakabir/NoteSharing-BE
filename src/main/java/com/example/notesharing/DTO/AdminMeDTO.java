package com.example.notesharing.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Identity of the signed-in admin, for the admin header + client-side admin gate. Returned 200 only
 * for ADMIN callers (the {@code /api/admin/**} security rule enforces this); a non-admin gets 403.
 */
@Getter
@Setter
@Builder
public class AdminMeDTO {

    private String email;
    private String name;
    private String role;
}

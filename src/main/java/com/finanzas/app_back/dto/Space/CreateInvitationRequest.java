package com.finanzas.app_back.dto.Space;

import lombok.Data;

@Data
public class CreateInvitationRequest {
    private String email;
    private String role;
}

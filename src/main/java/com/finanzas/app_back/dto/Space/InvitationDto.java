package com.finanzas.app_back.dto.Space;

import lombok.Data;

@Data
public class InvitationDto {
    private String code;
    private String spaceId;
    private String invitedEmail;
    private String role;
    private String status;
}

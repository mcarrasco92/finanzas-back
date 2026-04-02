package com.finanzas.app_back.model;

import lombok.Data;

@Data
public class Invitation {
    private String spaceId;
    private String invitedEmail;
    private String invitedBy;
    private String role;
    private String status; // "pending"
    private String code;  // 5-digit random code
}

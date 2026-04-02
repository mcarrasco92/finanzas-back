package com.finanzas.app_back.dto.Space;

import lombok.Data;

@Data
public class MemberDto {
    private String userId;
    private String name;
    private String role;
    private String joinedAt;
}

package com.finanzas.app_back.dto.Space;

import lombok.Data;

@Data
public class SpaceDto {
    private String spaceId;
    private String name;
    private String type;
    private String ownerId;
    private String role;
}

package com.finanzas.app_back.model;

import lombok.Data;

@Data
public class Space {
    private String name;
    private String type; // "personal" | "shared"
    private String ownerId;
    private String createdAt;
}

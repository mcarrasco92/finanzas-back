package com.finanzas.app_back.dto;

import lombok.Data;

@Data
public class GenericResponse {
    private String coderr;
    private String message;
    private Object data;
}

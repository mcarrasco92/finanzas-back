package com.finanzas.app_back.model;

import lombok.Data;

@Data
public class Categoria {
    private String id;
    private String nombre;
    private int orden;
    private boolean activa;
    private String tipo; // "ingreso" o "egreso"
}

package com.finanzas.app_back.model;

import lombok.Data;

@Data
public class Tarjeta {
    private String id;
    private String nombre;
    private String descripcion;
    private String institucion;
    private String dpago;
    private String dcorte;
    private int orden;
    private boolean activa;
}

package com.finanzas.app_back.model;

import lombok.Data;

@Data
public class Cuenta {
    private String id;
    private String nombre;
    private String descripcion;
    private String institucion;
    private double saldo;
    private boolean inversion;
    private boolean vista;
    private int orden;
    private boolean activa;
}

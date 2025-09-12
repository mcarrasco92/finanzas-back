package com.finanzas.app_back.dto.Cuentas;

import lombok.Data;

@Data
public class CuentaDto {
    private String id;
    private String nombre;
    private String descripcion;
    private String institucion;
    private double saldo;
    private boolean inversion;
    private boolean vista;
    private boolean activa;
    private int orden;

    public String validaCampos() {
        if (nombre == null || nombre.isEmpty()) return "El campo nombre es obligatorio.";
        if (descripcion == null || descripcion.isEmpty()) return "El campo descripcion es obligatorio.";
        if (institucion == null || institucion.isEmpty()) return "El campo institucion es obligatorio.";
        return "";
    }

}



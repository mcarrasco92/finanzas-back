package com.finanzas.app_back.dto.Categorias;

import lombok.Data;

@Data
public class CategoriaDto {
    private String id;
    private String nombre;
    private boolean activa;
    private int orden;
    private String tipo; // I - ingreso o  E - egreso
    private boolean transacciones;

    public String validaCampos() {
        if (nombre == null || nombre.isEmpty()) return "El campo nombre es obligatorio.";
        return "";
    }
}

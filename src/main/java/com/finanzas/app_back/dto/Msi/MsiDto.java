package com.finanzas.app_back.dto.Msi;

import lombok.Data;

@Data
public class MsiDto {
    private String id;
    private String fecha;
    private double importe;
    private String catEgresoId;
    private String tarjetaId;
    private String concepto;
    private String descripcion;
    private int meses;

    public String validaCampos() {
        if (fecha == null || fecha.isEmpty()) return "El campo fecha es obligatorio.";
        if (importe <= 0) return "El campo importe debe ser mayor a 0.";
        if (catEgresoId == null || catEgresoId.isEmpty()) return "El campo catEgresoId es obligatorio.";
        if (tarjetaId == null || tarjetaId.isEmpty()) return "El campo tarjetaId es obligatorio.";
        if (concepto == null || concepto.isEmpty()) return "El campo concepto es obligatorio.";
        if (meses <= 0) return "El campo meses debe ser mayor a 0.";    
        return "";
    }

}

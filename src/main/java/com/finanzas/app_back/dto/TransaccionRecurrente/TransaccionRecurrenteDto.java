package com.finanzas.app_back.dto.TransaccionRecurrente;

import lombok.Data;

@Data
public class TransaccionRecurrenteDto {
    private String id;
    private String fecha;
    private String dia;
    private double importe;
    private String catEgresoId;
    private String catIngresoId;
    private String tipo;
    private String tarjetaId;
    private String cuentaId;
    private String concepto;
    private String descripcion;
    private String necesario;
    private String periodicidad;

    public String validaCampos() {
        if (importe <= 0) return "El campo importe debe ser mayor a 0.";
        if (tipo == null || tipo.isEmpty()) return "El campo tipo es obligatorio.";
        if ((catEgresoId == null || catEgresoId.isEmpty()) && tipo == "Egreso" ) return "Debe proporcionar catEgresoId para transacciones de tipo Egreso.";
        if ((catIngresoId == null || catIngresoId.isEmpty()) && tipo == "Ingreso" ) return "Debe proporcionar catIngresoId para transacciones de tipo Ingreso.";
        if ((tarjetaId == null || tarjetaId.isEmpty()) && (cuentaId == null || cuentaId.isEmpty()) ) return "Debe proporcionar tarjetaId o cuentaId.";
        if (concepto == null || concepto.isEmpty()) return "El campo concepto es obligatorio.";
        if (periodicidad == null || periodicidad.isEmpty()) return "El campo periodicidad es obligatorio.";
        if ((fecha == null || fecha.isEmpty()) && periodicidad != "Semanal" ) return "El campo fecha es obligatorio.";
        if ((dia == null || dia.isEmpty()) && periodicidad == "Semanal" ) return "El campo día es obligatorio.";
        return "";
    }
}

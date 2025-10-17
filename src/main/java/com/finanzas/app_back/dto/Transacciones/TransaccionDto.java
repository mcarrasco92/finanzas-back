package com.finanzas.app_back.dto.Transacciones;

import lombok.Data;

@Data
public class TransaccionDto {
    private String id;
    private String fecha;
    private Double importe;
    private String catIngresoId;
    private String catEgresoId;
    private String cuentaId;
    private String tarjetaId;
    private String concepto;
    private String descripcion;
    private String tipo; // "Ingreso" o "Egreso"
    private String necesario; // Si o No
    private Boolean transferencia;
    private String msiId; // Si es una transaccion de tarjeta y es MSI, se guarda el id del MSI



    public String validaCampos() {
        if (fecha == null || fecha.isEmpty()) return "El campo fecha es obligatorio.";
        if (importe == null || importe <= 0) return "El campo importe es obligatorio y debe ser mayor que cero.";
        if ((catIngresoId == null || catIngresoId.isEmpty()) && (catEgresoId == null || catEgresoId.isEmpty()))
            return "Debe proporcionar una categoría de ingreso o de egreso.";
        if (cuentaId == null || cuentaId.isEmpty() && (tarjetaId == null || tarjetaId.isEmpty()))   
            return "El campo cuenta o tarjeta es obligatorio.";
        if (tipo == null || tipo.isEmpty() || (!tipo.equals("Ingreso") && !tipo.equals("Egreso")))
            return "El campo tipo es obligatorio y debe ser 'Ingreso' o 'Egreso'."; 
        return "";
    }
}

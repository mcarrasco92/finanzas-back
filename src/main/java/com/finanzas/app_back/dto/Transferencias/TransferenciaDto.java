package com.finanzas.app_back.dto.Transferencias;

import lombok.Data;

@Data
public class TransferenciaDto {
    private String id;
    private String fecha;
    private Double importe;
    private String tipoCuentaDestino; // "Cuenta" o "Tarjeta"
    private String cuentaOrigenId;
    private String cuentaDestinoId;
    private String concepto;

    public String validaCampos() {
        if (fecha == null || fecha.isEmpty()) return "El campo fecha es obligatorio.";
        if (importe == null || importe <= 0) return "El campo importe es obligatorio y debe ser mayor que cero.";
        if (cuentaOrigenId == null || cuentaOrigenId.isEmpty())
            return "El campo cuenta origen es obligatorio.";
        if (cuentaDestinoId == null || cuentaDestinoId.isEmpty())
            return "El campo cuenta destino es obligatorio.";
        if (cuentaOrigenId.equals(cuentaDestinoId))
            return "La cuenta origen y la cuenta destino no pueden ser la misma.";
        return "";
    }
}

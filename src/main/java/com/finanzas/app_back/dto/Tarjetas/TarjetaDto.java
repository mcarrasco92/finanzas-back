package com.finanzas.app_back.dto.Tarjetas;

import lombok.Data;

@Data
public class TarjetaDto {
    private String id;
    private String nombre;
    private String descripcion;
    private String institucion;
    private double saldo;
    private String dpago;
    private String dcorte;
    private boolean activa;
    private int orden;
    private boolean transacciones;
    private double pagoPendiente;
    private double saldoPeriodoActual;

    public String validaCampos() {
        if (nombre == null || nombre.isEmpty()) return "El campo nombre es obligatorio.";
        if (institucion == null || institucion.isEmpty()) return "El campo institución es obligatorio.";
        if (dpago == null || dpago.isEmpty()) return "El campo día de pago es obligatorio.";
        if (dcorte == null || dcorte.isEmpty()) return "El campo día de corte es obligatorio.";
        try {
            int dpagInt = Integer.parseInt(dpago);
            if (dpagInt < 1 || dpagInt > 28) return "El campo día de pago debe estar entre 1 y 28.";
        } catch (NumberFormatException e) {
            return "El campo día de pago debe ser un número válido.";
        }
        try {
            int dcorteInt = Integer.parseInt(dcorte);
            if (dcorteInt < 1 || dcorteInt > 28) return "El campo día de corte debe estar entre 1 y 28.";
        } catch (NumberFormatException e) {
            return "El campo día de corte debe ser un número válido.";
        }
        return "";
    }

}



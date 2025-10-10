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

    public String validaCampos() {
        if (nombre == null || nombre.isEmpty()) return "El campo nombre es obligatorio.";
        if (institucion == null || institucion.isEmpty()) return "El campo institución es obligatorio.";
        if (dpago == null || dpago.isEmpty()) return "El campo día de pago es obligatorio.";
        if (dcorte == null || dcorte.isEmpty()) return "El campo día de corte es obligatorio.";
        return "";
    }

}



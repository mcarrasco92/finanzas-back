package com.finanzas.app_back.dto.Cuentas;

import lombok.Data;

@Data
public class CuentaDto {
    private String nombre;
    private String descripcion;
    private String institucion;
    private boolean efectivo;
    private double saldo;
    private boolean inversion;
    private double tasa;
    private String periodicidad;

}

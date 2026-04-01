package com.finanzas.app_back.dto.Resumen;

import java.util.List;
import lombok.Data;

@Data
public class CuentaResumenDto {
    private String id;
    private String nombre;
    private String descripcion;
    private String institucion;
    private double saldo;
    private boolean inversion;
    private boolean vista;
    private boolean activa;
    private int orden;
    private List<TransaccionConCategoriaDto> transacciones;
}

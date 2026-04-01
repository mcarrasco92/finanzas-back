package com.finanzas.app_back.dto.Resumen;

import java.util.List;
import lombok.Data;

@Data
public class TarjetaResumenDto {
    private String id;
    private String nombre;
    private String descripcion;
    private String institucion;
    private double saldo;
    private String dpago;
    private String dcorte;
    private boolean activa;
    private int orden;
    private List<TransaccionConCategoriaDto> transacciones;
}

package com.finanzas.app_back.dto.Transacciones;

import lombok.Data;

@Data
public class TransaccionFiltroDto {
    private String yearMonth; // Año y mes en formato yyyy-MM
    private String tipo;      // Tipo de transacción (ingreso, egreso, etc.)
    private String categoria; // Categoría de la transacción (opcional)
    private String cuentaId; // ID de la cuenta (opcional)
    private String tarjetaId; // ID de la tarjeta (opcional)
    private String fechaInicio;
    private String fechaFin;
}

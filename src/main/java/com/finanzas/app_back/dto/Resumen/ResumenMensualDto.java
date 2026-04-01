package com.finanzas.app_back.dto.Resumen;

import java.util.List;
import lombok.Data;

@Data
public class ResumenMensualDto {
    private int mes;
    private int anio;
    private List<CuentaResumenDto> cuentas;
    private List<TarjetaResumenDto> tarjetas;
}

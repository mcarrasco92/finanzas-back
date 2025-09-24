package com.finanzas.app_back.dto.Cuentas;

import java.util.ArrayList;

import lombok.Data;

@Data
public class CuentasList {
    private ArrayList<CuentaDto> cuentas; // Listado de cuentas
    private Double saldoDisponible;
    private Double saldoInvertido;
    private Double saldoTotal;
}

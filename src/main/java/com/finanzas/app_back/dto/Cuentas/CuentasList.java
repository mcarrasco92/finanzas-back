package com.finanzas.app_back.dto.Cuentas;

import java.util.ArrayList;

import com.finanzas.app_back.model.Cuenta;

import lombok.Data;

@Data
public class CuentasList {
    private ArrayList<Cuenta> cuentas; // Listado de cuentas
    private Double saldoPagar;
    private Double saldoRestante;
    private Double saldoTotal;
}

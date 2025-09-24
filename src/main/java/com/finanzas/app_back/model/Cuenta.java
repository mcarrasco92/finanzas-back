package com.finanzas.app_back.model;

import com.finanzas.app_back.dto.Cuentas.CuentaDto;

import lombok.Data;

@Data
public class Cuenta {
    private String nombre;
    private String descripcion;
    private String institucion;
    private double saldo;
    private boolean inversion;
    private boolean vista;
    private int orden;
    private boolean activa;


    public void setDataDto(CuentaDto cuentaData) {
        this.nombre = cuentaData.getNombre();
        this.descripcion = cuentaData.getDescripcion();
        this.institucion = cuentaData.getInstitucion();
        this.saldo = cuentaData.getSaldo();
        this.inversion = cuentaData.isInversion();
        this.vista = cuentaData.isVista();
        this.orden = cuentaData.getOrden();
        this.activa = cuentaData.isActiva();
    }
}

package com.finanzas.app_back.model;

import com.finanzas.app_back.dto.Msi.MsiDto;

import lombok.Data;

@Data
public class Msi {
    private String fecha;
    private double importe;
    private String catEgresoId;
    private String tarjetaId;
    private String concepto;
    private String descripcion;
    private int meses;
    public void setData(MsiDto msiData) {
        this.fecha = msiData.getFecha();
        this.importe = msiData.getImporte();
        this.catEgresoId = msiData.getCatEgresoId();
        this.tarjetaId = msiData.getTarjetaId();
        this.concepto = msiData.getConcepto();
        this.descripcion = msiData.getDescripcion();
        this.meses = msiData.getMeses();
    }
}

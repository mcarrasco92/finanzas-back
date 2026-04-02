package com.finanzas.app_back.model;

import com.finanzas.app_back.dto.Transferencias.TransferenciaDto;

import lombok.Data;

@Data
public class Transferencia {
    private String fecha;
    private Double importe;
    private String tipoCuentaDestino; // "Cuenta" o "Tarjeta"
    private String cuentaOrigenId;
    private String cuentaDestinoId;
    private String concepto;
    private String nombreCuentaOrigen;
    private String nombreCuentaDestino;

    public void setDataDto(TransferenciaDto transferenciaData) {
        this.fecha = transferenciaData.getFecha();
        this.importe = transferenciaData.getImporte();
        this.tipoCuentaDestino = transferenciaData.getTipoCuentaDestino();
        this.cuentaOrigenId = transferenciaData.getCuentaOrigenId();
        this.cuentaDestinoId = transferenciaData.getCuentaDestinoId();
        this.concepto = transferenciaData.getConcepto();
    }
}

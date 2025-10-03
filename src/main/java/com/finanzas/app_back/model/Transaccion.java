package com.finanzas.app_back.model;

import com.finanzas.app_back.dto.Transacciones.TransaccionDto;

import lombok.Data;

@Data
public class Transaccion {
    private String fecha;
    private Double importe;
    private String catIngresoId;
    private String catEgresoId;
    private String cuentaId;
    private String tarjetaId;
    private String concepto;
    private String descripcion;
    private String tipo; // "Ingreso" o "Egreso"
    private String necesario;

    public void setDataDto(TransaccionDto transaccionData) {
        this.fecha = transaccionData.getFecha();
        this.importe = transaccionData.getImporte();
        this.catIngresoId = transaccionData.getCatIngresoId();
        this.catEgresoId = transaccionData.getCatEgresoId();
        this.cuentaId = transaccionData.getCuentaId();
        this.tarjetaId = transaccionData.getTarjetaId();
        this.concepto = transaccionData.getConcepto();
        this.descripcion = transaccionData.getDescripcion();
        this.tipo = transaccionData.getTipo();
        this.necesario = transaccionData.getNecesario();
    }   

}

package com.finanzas.app_back.model;

import com.finanzas.app_back.dto.TransaccionRecurrente.TransaccionRecurrenteDto;

import lombok.Data;

@Data
public class TransaccionRecurrente {
    private String fecha;
    private String dia;
    private double importe;
    private String tipo; // Ingreso o Egreso
    private String catEgresoId;
    private String catIngresoId;
    private String tarjetaId;
    private String cuentaId;
    private String concepto;
    private String descripcion;
    private String necesario;
    private String periodicidad; // Semanal(día de la semana)/Mensual/Bimestral/Semestral/Anual
    private String siguienteEjecucion;

    public void setData(TransaccionRecurrenteDto dto) {
        this.fecha = dto.getFecha();
        this.dia = dto.getDia();
        this.importe = dto.getImporte();
        this.catEgresoId = dto.getCatEgresoId();
        this.catIngresoId = dto.getCatIngresoId();
        this.tipo = dto.getTipo();
        this.tarjetaId = dto.getTarjetaId();
        this.cuentaId = dto.getCuentaId();
        this.concepto = dto.getConcepto();
        this.descripcion = dto.getDescripcion();
        this.necesario = dto.getNecesario();
        this.periodicidad = dto.getPeriodicidad();
    }   
}

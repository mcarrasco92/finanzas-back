package com.finanzas.app_back.model;
import com.finanzas.app_back.dto.Tarjetas.TarjetaDto;

import lombok.Data;

@Data
public class Tarjeta {
    private String nombre;
    private String descripcion;
    private String institucion;
    private String dpago;
    private String dcorte;
    private int orden;
    private boolean activa;


    public void setDataDto(TarjetaDto tarjetaData) {
        this.nombre = tarjetaData.getNombre();
        this.descripcion = tarjetaData.getDescripcion();
        this.institucion = tarjetaData.getInstitucion();
        this.dpago = tarjetaData.getDpago();
        this.dcorte = tarjetaData.getDcorte();
        this.orden = tarjetaData.getOrden();
        this.activa = tarjetaData.isActiva();
    }
}

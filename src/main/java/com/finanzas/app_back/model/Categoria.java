package com.finanzas.app_back.model;

import com.finanzas.app_back.dto.Categorias.CategoriaDto;

import lombok.Data;

@Data
public class Categoria {
    private String nombre;
    private int orden;
    private boolean activa;
    private String tipo; // "ingreso" o "egreso"

    public void setDataDto(CategoriaDto categoriaData) {
        this.nombre = categoriaData.getNombre();
        this.orden = categoriaData.getOrden();
        this.activa = categoriaData.isActiva();
        this.tipo = categoriaData.getTipo();
    }
}

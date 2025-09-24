package com.finanzas.app_back.dto.Categorias;

import lombok.Data;

import java.util.ArrayList;

@Data
public class CategoriasList {
    private ArrayList<CategoriaDto> categoriasIngresos;
    private ArrayList<CategoriaDto> categoriasEgresos;

}

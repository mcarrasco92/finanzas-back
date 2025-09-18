package com.finanzas.app_back.dto.Categorias;
import com.finanzas.app_back.model.Categoria;

import lombok.Data;

import java.util.ArrayList;

@Data
public class CategoriasList {
    private ArrayList<Categoria> categoriasIngresos;
    private ArrayList<Categoria> categoriasEgresos;

}

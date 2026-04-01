package com.finanzas.app_back.dto.Resumen;

import com.finanzas.app_back.dto.Categorias.CategoriaDto;
import lombok.Data;

@Data
public class TransaccionConCategoriaDto {
    private String id;
    private String fecha;
    private Double importe;
    private String catIngresoId;
    private String catEgresoId;
    private String cuentaId;
    private String tarjetaId;
    private String concepto;
    private String descripcion;
    private String tipo;
    private Boolean transferencia;
    private String msiId;
    private CategoriaDto categoria;
}

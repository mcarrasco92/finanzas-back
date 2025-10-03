package com.finanzas.app_back.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Categorias.CategoriaDto;
import com.finanzas.app_back.dto.Categorias.CategoriasList;
import com.finanzas.app_back.model.Categoria;
import com.finanzas.app_back.repositories.CategoriasRepository;

@Service
public class CategoriasService {

 @Autowired
    private GeneralService generalService;

    @Autowired
    private CategoriasRepository categoriasRepository;

    private GenericResponse response = new GenericResponse();


    public GenericResponse registrarCategoria(String uid ,CategoriaDto dto) {

        try {

            Categoria categoria = new Categoria();
            categoria.setDataDto(dto);

            String categoriaId = categoriasRepository.newCategoria(uid, categoria);
            dto.setId(categoriaId);

            response.setCoderr("0000");
            response.setMessage("Categoria registrada exitosamente.");
            response.setData(dto);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al registrar la categoria");
        }

        return response;
    }


    public GenericResponse obtenerCategorias(String uid) {
        
    
        try {

            ArrayList<CategoriaDto> categorias = categoriasRepository.getCategorias(uid);

            if (categorias.isEmpty()) {
                response.setCoderr("0001");
                response.setMessage("No se encontraron categorias.");
                return response;
            }

            CategoriasList categoriasList = separarCategoriasPorTipo(categorias);


            response.setCoderr("0000");
            response.setMessage("Categorias obtenidas exitosamente.");
            response.setData(categoriasList);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener las categorias");
        }

        return response;
    }


    public GenericResponse consultaCategoria(String uid, String categoriaId) {

        try {

            CategoriaDto categoria = categoriasRepository.getCategoriaById(uid, categoriaId);

            if(categoria == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            }

            response.setCoderr("0000");
            response.setMessage("Categoria obtenida exitosamente.");
            response.setData(categoria);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener la categoria");
        }

        return response;
    }


    public GenericResponse actualizarCategoria(String uid, String categoriaId, CategoriaDto updatedCategoriaDto) {

        try {

            CategoriaDto existingCategoriaDto = categoriasRepository.getCategoriaById(uid, categoriaId);

            if(existingCategoriaDto == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            } 

            existingCategoriaDto.setNombre(updatedCategoriaDto.getNombre());
            existingCategoriaDto.setTipo(updatedCategoriaDto.getTipo());


            Categoria categoria = new Categoria();
            categoria.setDataDto(existingCategoriaDto);

            categoriasRepository.updateCategoria(uid, categoriaId, categoria);

            response.setCoderr("0000");
            response.setMessage("Categoria actualizada exitosamente.");
            response.setData(existingCategoriaDto);

            
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar la categoria");
        }

        return response;
    }


    public GenericResponse eliminarCategoria(String uid, String categoriaId) {

        try {
            
            CategoriaDto categoria = categoriasRepository.getCategoriaById(uid, categoriaId);

            if(categoria == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            }

            if(categoria.isActiva()){
                response.setCoderr("0002");
                response.setMessage("No se puede eliminar una categoria activa.");
                return response;
            }   

            categoriasRepository.deleteCategoria(uid, categoriaId);

            response.setCoderr("0000");
            response.setMessage("Categoria eliminada exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al eliminar la categoria");
        }

        return response;
    }
    

    public GenericResponse ordenCategorias(String uid, ArrayList<CategoriaDto> categorias){

        int orden = 1;
        String categoriaId = "";

        try {

        for (CategoriaDto categoriaFor : categorias) {

            categoriaId = categoriaFor.getId();
            
            if (categoriaId != null) { 
                CategoriaDto categoriaDto = categoriasRepository.getCategoriaById(uid, categoriaId);

                if(categoriaDto != null){
                    categoriaDto.setOrden(orden);
                    orden++;

                    Categoria categoriaToUpdate = new Categoria();
                    categoriaToUpdate.setDataDto(categoriaDto);
                    categoriasRepository.updateCategoria(uid, categoriaId, categoriaToUpdate);
                }
                
            }
        }

        response.setCoderr("0000");
        response.setMessage("Categorias ordenadas exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al ordenar las categorias");
        }

        return response;
    }



    public GenericResponse activarCategoria(String uid, String categoriaId, boolean activa) {

        try {


            CategoriaDto categoriaDto = categoriasRepository.getCategoriaById(uid, categoriaId);

            if(categoriaDto == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            }

            categoriaDto.setActiva(activa);


            Categoria categoriaToUpdate = new Categoria();
            categoriaToUpdate.setDataDto(categoriaDto);
            categoriasRepository.updateCategoria(uid, categoriaId, categoriaToUpdate);

            response.setCoderr("0000");
            response.setMessage("Categoria actualizada exitosamente.");
            response.setData(activa);

            
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar la categoria");
        }

        return response;
    }















    private CategoriasList separarCategoriasPorTipo(ArrayList<CategoriaDto> categorias) {
        ArrayList<CategoriaDto> categoriasIngresos = new ArrayList<>();
        ArrayList<CategoriaDto> categoriasEgresos = new ArrayList<>();

        for (CategoriaDto categoria : categorias) {
            if ("I".equals(categoria.getTipo())) {
                categoriasIngresos.add(categoria);
            } else if ("E".equals(categoria.getTipo())) {
                categoriasEgresos.add(categoria);
            }
        }

        CategoriasList categoriasList = new CategoriasList();
        categoriasList.setCategoriasIngresos(categoriasIngresos);
        categoriasList.setCategoriasEgresos(categoriasEgresos);

        return categoriasList;
    }

}

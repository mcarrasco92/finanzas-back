package com.finanzas.app_back.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Categorias.CategoriaDto;
import com.finanzas.app_back.dto.Categorias.CategoriasList;
import com.finanzas.app_back.model.Categoria;
import com.finanzas.app_back.repositories.CategoriasRepository;
import com.finanzas.app_back.repositories.SpaceRepository;

@Service
public class CategoriasService {

 @Autowired
    private GeneralService generalService;

    @Autowired
    private CategoriasRepository categoriasRepository;
    @Autowired
    private SpaceRepository spaceRepository;

    private GenericResponse response = new GenericResponse();


    public GenericResponse registrarCategoria(String spaceId, String uid, CategoriaDto dto) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            Categoria categoria = new Categoria();
            categoria.setDataDto(dto);

            String categoriaId = categoriasRepository.newCategoria(spaceId, categoria);
            dto.setId(categoriaId);

            response.setCoderr("0000");
            response.setMessage("Categoria registrada exitosamente.");
            response.setData(dto);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al registrar la categoria");
        }

        return response;
    }


    public GenericResponse obtenerCategorias(String spaceId, String uid) {


        try {

            spaceRepository.validateMembership(spaceId, uid);

            ArrayList<CategoriaDto> categorias = categoriasRepository.getCategorias(spaceId);

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


    public GenericResponse consultaCategoria(String spaceId, String uid, String categoriaId) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            CategoriaDto categoria = categoriasRepository.getCategoriaById(spaceId, categoriaId);

            if(categoria == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            }

            categoria.setTransacciones(categoriasRepository.getExistTransaccionesByCat(spaceId, categoriaId));

            response.setCoderr("0000");
            response.setMessage("Categoria obtenida exitosamente.");
            response.setData(categoria);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener la categoria");
        }

        return response;
    }


    public GenericResponse actualizarCategoria(String spaceId, String uid, String categoriaId, CategoriaDto updatedCategoriaDto) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            CategoriaDto existingCategoriaDto = categoriasRepository.getCategoriaById(spaceId, categoriaId);

            if(existingCategoriaDto == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            }

            existingCategoriaDto.setNombre(updatedCategoriaDto.getNombre());
            existingCategoriaDto.setTipo(updatedCategoriaDto.getTipo());


            Categoria categoria = new Categoria();
            categoria.setDataDto(existingCategoriaDto);

            categoriasRepository.updateCategoria(spaceId, categoriaId, categoria);

            response.setCoderr("0000");
            response.setMessage("Categoria actualizada exitosamente.");
            response.setData(existingCategoriaDto);


        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar la categoria");
        }

        return response;
    }


    public GenericResponse eliminarCategoria(String spaceId, String uid, String categoriaId) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            CategoriaDto categoria = categoriasRepository.getCategoriaById(spaceId, categoriaId);

            if(categoria == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            }

            if(categoriasRepository.getExistTransaccionesByCat(spaceId, categoriaId)){
                response.setCoderr("0002");
                response.setMessage("No se puede eliminar la categoria porque tiene transacciones asociadas.");
                return response;
            }

            categoriasRepository.deleteCategoria(spaceId, categoriaId);

            response.setCoderr("0000");
            response.setMessage("Categoria eliminada exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al eliminar la categoria");
        }

        return response;
    }


    public GenericResponse ordenCategorias(String spaceId, String uid, ArrayList<CategoriaDto> categorias){

        int orden = 1;
        String categoriaId = "";

        try {

            spaceRepository.validateMembership(spaceId, uid);

        for (CategoriaDto categoriaFor : categorias) {

            categoriaId = categoriaFor.getId();

            if (categoriaId != null) {
                CategoriaDto categoriaDto = categoriasRepository.getCategoriaById(spaceId, categoriaId);

                if(categoriaDto != null){
                    categoriaDto.setOrden(orden);
                    orden++;

                    Categoria categoriaToUpdate = new Categoria();
                    categoriaToUpdate.setDataDto(categoriaDto);
                    categoriasRepository.updateCategoria(spaceId, categoriaId, categoriaToUpdate);
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



    public GenericResponse activarCategoria(String spaceId, String uid, String categoriaId, boolean activa) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            CategoriaDto categoriaDto = categoriasRepository.getCategoriaById(spaceId, categoriaId);

            if(categoriaDto == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            }

            categoriaDto.setActiva(activa);


            Categoria categoriaToUpdate = new Categoria();
            categoriaToUpdate.setDataDto(categoriaDto);
            categoriasRepository.updateCategoria(spaceId, categoriaId, categoriaToUpdate);

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

package com.finanzas.app_back.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.finanzas.app_back.service.CategoriasService;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Categorias.CategoriaDto;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/categorias")
public class CategoriasController {

    @Autowired
    private CategoriasService categoriasService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getCategorias(HttpServletRequest request) {

        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");
            response = categoriasService.obtenerCategorias(uid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al recuperar las categorias");
        }

    }


    ///////////////////////////////     REGISTRTA CUENTA    //////////////////////////////

    @PostMapping("/registrar")
    public ResponseEntity<GenericResponse> registrarCategoria(HttpServletRequest request, @RequestBody CategoriaDto categoriaData) {
        try {
            String uid = (String) request.getAttribute("uid");

            String valida = categoriaData.validaCampos();
            if (!valida.isEmpty()) {
                GenericResponse response = new GenericResponse();
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            categoriaData.setActiva(true);
            GenericResponse response = categoriasService.registrarCategoria(uid, categoriaData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al registrar la categoría");
        }
    }


    ///////////////////////////////     ELIMINAR CUENTA    //////////////////////////////

    @DeleteMapping("/eliminar/{categoriaId}")
    public ResponseEntity<GenericResponse> eliminarCategoria(HttpServletRequest request, @PathVariable String categoriaId) {
        ResponseEntity<GenericResponse> validacion = validarCategoriaId(categoriaId);
        if (validacion != null) return validacion;

        try {
            String uid = (String) request.getAttribute("uid");
            GenericResponse response = categoriasService.eliminarCategoria(uid, categoriaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al eliminar la categoría");
        }
    }


    ///////////////////////////////     ACTUALIZAR CUENTA    //////////////////////////////

    @PutMapping("/actualizar/{categoriaId}")
    public ResponseEntity<GenericResponse> actualizarCategoria(HttpServletRequest request, @PathVariable String categoriaId, @RequestBody CategoriaDto updatedCategoriaData) {
        ResponseEntity<GenericResponse> validacion = validarCategoriaId(categoriaId);
        if (validacion != null) return validacion;

        try {
            String uid = (String) request.getAttribute("uid");

            String valida = updatedCategoriaData.validaCampos();
            if (!valida.isEmpty()) {
                GenericResponse response = new GenericResponse();
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            GenericResponse response = categoriasService.actualizarCategoria(uid, categoriaId, updatedCategoriaData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al actualizar la categoría");
        }
    }

    ///////////////////////////////     CONSULTA CUENTA    //////////////////////////////

    @GetMapping("/{categoriaId}")
    public ResponseEntity<GenericResponse> consultaCategoria(HttpServletRequest request, @PathVariable String categoriaId) {
        ResponseEntity<GenericResponse> validacion = validarCategoriaId(categoriaId);
        if (validacion != null) return validacion;

        try {
            String uid = (String) request.getAttribute("uid");
            GenericResponse response = categoriasService.consultaCategoria(uid, categoriaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al consultar la categoría");
        }
    }

    ///////////////////////////////     ORDENAR CUENTAS    //////////////////////////////
    @PostMapping("/orden")
    public ResponseEntity<GenericResponse> ordenCategorias(HttpServletRequest request, @RequestBody ArrayList<CategoriaDto> categoriasList) {
        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");
            response = categoriasService.ordenCategorias(uid, categoriasList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al ordenar las categorias");
        }

    }

    ///////////////////////////////     ACTIVAR / DESACTIVAR CUENTA    //////////////////////////////
    @PutMapping("/activar/{categoriaId}")
    public ResponseEntity<GenericResponse> activarCategoria(HttpServletRequest request, @PathVariable String categoriaId, @RequestBody Boolean activa) {
        ResponseEntity<GenericResponse> validacion = validarCategoriaId(categoriaId);
        if (validacion != null) return validacion;

        try {
            String uid = (String) request.getAttribute("uid");
            GenericResponse response = categoriasService.activarCategoria(uid, categoriaId, activa);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al activar la categoría");
        }
    }



    private ResponseEntity<GenericResponse> validarCategoriaId(String categoriaId) {
        GenericResponse response = new GenericResponse();
        if (categoriaId == null || categoriaId.isEmpty()) {
            response.setCoderr("1002");
            response.setMessage("El ID de la categoría es obligatorio.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return null; // Indica que la validación fue exitosa
    }

    private ResponseEntity<GenericResponse> manejarExcepcion(Exception e, String mensaje) {
        GenericResponse response = new GenericResponse();
        response.setCoderr("9999");
        response.setMessage(mensaje + ": " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }








}

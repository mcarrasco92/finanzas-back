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
            response.setCoderr("9999");
            response.setMessage("Error al recuperar las categorias: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }


    ///////////////////////////////     REGISTRTA CUENTA    //////////////////////////////

    @PostMapping("/registrar")
    public ResponseEntity<GenericResponse> registrarCategoria(HttpServletRequest request, @RequestBody CategoriaDto categoriaData) {

        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");

            String valida = categoriaData.validaCampos();

            if(!valida.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            categoriaData.setActiva(true);

            // Llamar al servicio para registrar la categoria
            response = categoriasService.registrarCategoria(uid, categoriaData);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al registrar la categoria: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    ///////////////////////////////     ELIMINAR CUENTA    //////////////////////////////

    @DeleteMapping("/eliminar/{categoriaId}")
    public ResponseEntity<GenericResponse> eliminarCategoria(HttpServletRequest request, @PathVariable String categoriaId) {
        
        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");

            if (categoriaId == null || categoriaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la categoria es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response = categoriasService.eliminarCategoria(uid, categoriaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al eliminar la categoria: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    ///////////////////////////////     ACTUALIZAR CUENTA    //////////////////////////////

    @PutMapping("/actualizar/{categoriaId}")
    public ResponseEntity<GenericResponse> actualizarCategoria(HttpServletRequest request, @PathVariable String categoriaId, @RequestBody CategoriaDto updatedCategoriaData) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");

            if (categoriaId == null || categoriaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la categoria es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            String valida = updatedCategoriaData.validaCampos();
            if(!valida.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            response = categoriasService.actualizarCategoria(uid, categoriaId, updatedCategoriaData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al actualizar la categoria: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    ///////////////////////////////     CONSULTA CUENTA    //////////////////////////////

    @GetMapping("/{categoriaId}")
    public ResponseEntity<GenericResponse> consultaCategoria(HttpServletRequest request, @PathVariable String categoriaId) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");

            if (categoriaId == null || categoriaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la categoria es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response = categoriasService.consultaCategoria(uid, categoriaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al consultar la categoria: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    ///////////////////////////////     ORDENAR CUENTAS    //////////////////////////////
    @PostMapping("/orden")
    public ResponseEntity<GenericResponse> ordenCategorias(HttpServletRequest request, @RequestBody ArrayList<CategoriaDto> categoriasList) {
        GenericResponse response = new GenericResponse();

        try {

            String uid = (String) request.getAttribute("uid");

            response = categoriasService.ordenCategorias(uid, categoriasList);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al ordenar las categoria: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        return ResponseEntity.ok(response);
    }

    ///////////////////////////////     ACTIVAR / DESACTIVAR CUENTA    //////////////////////////////
    @PutMapping("/activar/{categoriaId}")
    public ResponseEntity<GenericResponse> activarCategoria(HttpServletRequest request, @PathVariable String categoriaId, @RequestBody boolean activa) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");

            if (categoriaId == null || categoriaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la categoria es obligatorio.");
                return ResponseEntity.ok(response);
            }

            response = categoriasService.activarCategoria(uid, categoriaId, activa);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al activar la categoria: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}

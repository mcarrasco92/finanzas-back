package com.finanzas.app_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Transacciones.TransaccionDto;
import com.finanzas.app_back.dto.Transacciones.TransaccionFiltroDto;
import com.finanzas.app_back.service.TransaccionesService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionesController {

    @Autowired
    private TransaccionesService transaccionesService;

    @PostMapping("getTransacciones")
    public ResponseEntity<GenericResponse> getTransacciones(HttpServletRequest request, @RequestBody TransaccionFiltroDto filtroDto) {

        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");

            response = transaccionesService.obtenerTransacciones(uid, filtroDto);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al recuperar las transacciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }


    ///////////////////////////////     REGISTRTA CUENTA    //////////////////////////////

    @PostMapping("/registrar")
    public ResponseEntity<GenericResponse> registrarTransaccion(HttpServletRequest request, @RequestBody TransaccionDto transaccionData) {

        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");

            String valida = transaccionData.validaCampos();

            if(!valida.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            // Llamar al servicio para registrar la transaccion
            response = transaccionesService.registrarTransaccion(uid, transaccionData);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al registrar la transaccion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    ///////////////////////////////     ELIMINAR CUENTA    //////////////////////////////

    @DeleteMapping("/eliminar/{transaccionId}")
    public ResponseEntity<GenericResponse> eliminarTransaccion(HttpServletRequest request, @PathVariable String transaccionId) {
        
        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");

            if (transaccionId == null || transaccionId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la transaccion es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response = transaccionesService.eliminarTransaccion(uid, transaccionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al eliminar la transaccion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    ///////////////////////////////     ACTUALIZAR CUENTA    //////////////////////////////

    @PutMapping("/actualizar/{transaccionId}")
    public ResponseEntity<GenericResponse> actualizarTransaccion(HttpServletRequest request, @PathVariable String transaccionId, @RequestBody TransaccionDto updatedTransaccionData) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");

            if (transaccionId == null || transaccionId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la transaccion es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            String valida = updatedTransaccionData.validaCampos();
            if(!valida.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            response = transaccionesService.actualizarTransaccion(uid, transaccionId, updatedTransaccionData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al actualizar la transaccion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    ///////////////////////////////     CONSULTA CUENTA    //////////////////////////////

    @GetMapping("/{transaccionId}")
    public ResponseEntity<GenericResponse> consultaTransaccion(HttpServletRequest request, @PathVariable String transaccionId) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");

            if (transaccionId == null || transaccionId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la transaccion es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response = transaccionesService.consultaTransaccion(uid, transaccionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al consultar la transaccion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



}

package com.finanzas.app_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.TransaccionRecurrente.TransaccionRecurrenteDto;
import com.finanzas.app_back.service.TransaccionRecurrenteService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/transaccion-recurrente")
public class TransaccionRecurrenteController {
    @Autowired
    private TransaccionRecurrenteService service;

    @PostMapping("/registrar")
    public ResponseEntity<GenericResponse> registrar(HttpServletRequest request, @RequestBody TransaccionRecurrenteDto dto) {
        try {
            String uid = (String) request.getAttribute("uid");
            String valida = dto.validaCampos();
            if (!valida.isEmpty()) {
                GenericResponse response = new GenericResponse();
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.ok(service.registrarTransaccionRecurrente(uid, dto));
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al registrar la transacción recurrente");
        }
    }

    @GetMapping("")
    public ResponseEntity<GenericResponse> obtenerTodos(HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");
            return ResponseEntity.ok(service.obtenerTransaccionesRecurrentes(uid));
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al obtener las transacciones recurrentes");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenericResponse> obtenerPorId(HttpServletRequest request, @PathVariable String id) {
        try {
            String uid = (String) request.getAttribute("uid");
            return ResponseEntity.ok(service.obtenerTransaccionRecurrenteById(uid, id));
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al obtener la transacción recurrente por ID");
        }
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<GenericResponse> eliminar(HttpServletRequest request, @PathVariable String id) {
        try {
            String uid = (String) request.getAttribute("uid");
            return ResponseEntity.ok(service.eliminarTransaccionRecurrente(uid, id));
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al eliminar la transacción recurrente");
        }
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<GenericResponse> actualizar(HttpServletRequest request, @PathVariable String id, @RequestBody TransaccionRecurrenteDto dto) {
        try {
            String uid = (String) request.getAttribute("uid");
            String valida = dto.validaCampos();
            if (!valida.isEmpty()) {
                GenericResponse response = new GenericResponse();
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.ok(service.actualizarTransaccionRecurrente(uid, id, dto));
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al actualizar la transacción recurrente");
        }
    }

    private ResponseEntity<GenericResponse> manejarExcepcion(Exception e, String mensaje) {
        GenericResponse response = new GenericResponse();
        response.setCoderr("9999");
        response.setMessage(mensaje + ": " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}

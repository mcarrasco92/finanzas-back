package com.finanzas.app_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Transferencias.TransferenciaDto;
import com.finanzas.app_back.service.TransferenciasService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/transferencias")
public class TransferenciasController {

    @Autowired
    private TransferenciasService transferenciasService;

    @PostMapping("/registrar")
    public ResponseEntity<GenericResponse> registrarTransferencia(HttpServletRequest request, @RequestBody TransferenciaDto transferenciaData) {
        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            String valida = transferenciaData.validaCampos();

            if(!valida.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            // Llamar al servicio para registrar la transaccion
            response = transferenciasService.registrarTransferencia(spaceId, uid, transferenciaData);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al registrar la transferencia: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }


    }

    @GetMapping("/{transferenciaId}")
    public ResponseEntity<GenericResponse> getTransferenciaById(HttpServletRequest request, @PathVariable String transferenciaId) {
        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            // Llamar al servicio para obtener la transferencia por ID
            response = transferenciasService.getTransferenciaById(spaceId, uid, transferenciaId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al recuperar la transferencia: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    };

    @DeleteMapping("/eliminar/{transferenciaId}")
    public ResponseEntity<GenericResponse> eliminarTransferencia(HttpServletRequest request, @PathVariable String transferenciaId) {
        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            // Llamar al servicio para eliminar la transferencia por ID
            response = transferenciasService.eliminarTransferencia(spaceId, uid, transferenciaId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al eliminar la transferencia: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    };

    @PutMapping("/actualizar/{transferenciaId}")
    public ResponseEntity<GenericResponse> actualizarTransferencia(HttpServletRequest request, @PathVariable String transferenciaId, @RequestBody TransferenciaDto updatedData) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            String valida = updatedData.validaCampos();

            if(!valida.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            // Llamar al servicio para actualizar la transferencia
            response = transferenciasService.actualizarTransferencia(spaceId, uid, transferenciaId, updatedData);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al actualizar la transferencia: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    };


}

package com.finanzas.app_back.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.finanzas.app_back.service.CuentasService;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Cuentas.CuentaDto;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;

//////////////////////////////     GET CUENTAS    //////////////////////////////


@RestController
@RequestMapping("/api/cuentas")
public class CuentasController {

    @Autowired
    private CuentasService cuentasService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getCuentas(HttpServletRequest request) {

        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            response = cuentasService.obtenerCuentas(spaceId, uid);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al recuperar las cuentas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }


    ///////////////////////////////     REGISTRTA CUENTA    //////////////////////////////

    @PostMapping("/registrar")
    public ResponseEntity<GenericResponse> registrarCuenta(HttpServletRequest request, @RequestBody CuentaDto cuentaData) {

        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            String valida = cuentaData.validaCampos();

            if(!valida.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            // Llamar al servicio para registrar la cuenta
            response = cuentasService.registrarCuenta(spaceId, uid, cuentaData);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al registrar la cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    ///////////////////////////////     ELIMINAR CUENTA    //////////////////////////////

    @DeleteMapping("/eliminar/{cuentaId}")
    public ResponseEntity<GenericResponse> eliminarCuenta(HttpServletRequest request, @PathVariable String cuentaId) {

        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            if (cuentaId == null || cuentaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la cuenta es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response = cuentasService.eliminarCuenta(spaceId, uid, cuentaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al eliminar la cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    ///////////////////////////////     ACTUALIZAR CUENTA    //////////////////////////////

    @PutMapping("/actualizar/{cuentaId}")
    public ResponseEntity<GenericResponse> actualizarCuenta(HttpServletRequest request, @PathVariable String cuentaId, @RequestBody CuentaDto updatedCuentaData) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            if (cuentaId == null || cuentaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la cuenta es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            String valida = updatedCuentaData.validaCampos();
            if(!valida.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            response = cuentasService.actualizarCuenta(spaceId, uid, cuentaId, updatedCuentaData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al actualizar la cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    ///////////////////////////////     CONSULTA CUENTA    //////////////////////////////

    @GetMapping("/{cuentaId}")
    public ResponseEntity<GenericResponse> consultaCuenta(HttpServletRequest request, @PathVariable String cuentaId) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            if (cuentaId == null || cuentaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la cuenta es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response = cuentasService.consultaCuenta(spaceId, uid, cuentaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al consultar la cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    ///////////////////////////////     ORDENAR CUENTAS    //////////////////////////////
    @PostMapping("/orden")
    public ResponseEntity<GenericResponse> ordenCuentas(HttpServletRequest request, @RequestBody ArrayList<CuentaDto> cuentasList) {
        GenericResponse response = new GenericResponse();

        try {

            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            response = cuentasService.ordenCuentas(spaceId, uid, cuentasList);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al ordenar las cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        return ResponseEntity.ok(response);
    }

    ///////////////////////////////     ACTIVAR / DESACTIVAR CUENTA    //////////////////////////////
    @PutMapping("/activar/{cuentaId}")
    public ResponseEntity<GenericResponse> activarCuenta(HttpServletRequest request, @PathVariable String cuentaId, @RequestBody boolean activa) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            if (cuentaId == null || cuentaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la cuenta es obligatorio.");
                return ResponseEntity.ok(response);
            }

            response = cuentasService.activarCuenta(spaceId, uid, cuentaId, activa);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al activar la cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}

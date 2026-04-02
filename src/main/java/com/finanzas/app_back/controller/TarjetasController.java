package com.finanzas.app_back.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.finanzas.app_back.service.TarjetasService;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Tarjetas.TarjetaDto;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;

//////////////////////////////     GET TARJETAS    //////////////////////////////


@RestController
@RequestMapping("/api/tarjetas")
public class TarjetasController {

    @Autowired
    private TarjetasService tarjetasService;

    @GetMapping("")
    public ResponseEntity<GenericResponse> getTarjetas(HttpServletRequest request) {

        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            response = tarjetasService.obtenerTarjetas(spaceId, uid);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al recuperar las tarjetas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }


    ///////////////////////////////     REGISTRAR TARJETA    //////////////////////////////

    @PostMapping("/registrar")
    public ResponseEntity<GenericResponse> registrarTarjeta(HttpServletRequest request, @RequestBody TarjetaDto tarjetaData) {

        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            String valida = tarjetaData.validaCampos();

            if(!valida.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            // Llamar al servicio para registrar la tarjeta
            response = tarjetasService.registrarTarjeta(spaceId, uid, tarjetaData);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al registrar la tarjeta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    ///////////////////////////////     ELIMINAR TARJETA    //////////////////////////////

    @DeleteMapping("/eliminar/{tarjetaId}")
    public ResponseEntity<GenericResponse> eliminarTarjeta(HttpServletRequest request, @PathVariable String tarjetaId) {

        GenericResponse response = new GenericResponse();

        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            if (tarjetaId == null || tarjetaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la tarjeta es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response = tarjetasService.eliminarTarjeta(spaceId, uid, tarjetaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al eliminar la tarjeta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    ///////////////////////////////     ACTUALIZAR TARJETA    //////////////////////////////

    @PutMapping("/actualizar/{tarjetaId}")
    public ResponseEntity<GenericResponse> actualizarTarjeta(HttpServletRequest request, @PathVariable String tarjetaId, @RequestBody TarjetaDto updatedTarjetaData) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            if (tarjetaId == null || tarjetaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la tarjeta es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            String valida = updatedTarjetaData.validaCampos();
            if(!valida.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            response = tarjetasService.actualizarTarjeta(spaceId, uid, tarjetaId, updatedTarjetaData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al actualizar la tarjeta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    ///////////////////////////////     CONSULTA TARJETA    //////////////////////////////

    @GetMapping("/{tarjetaId}")
    public ResponseEntity<GenericResponse> consultaTarjeta(HttpServletRequest request, @PathVariable String tarjetaId) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            if (tarjetaId == null || tarjetaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la tarjeta es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            response = tarjetasService.consultaTarjeta(spaceId, uid, tarjetaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al consultar la tarjeta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    ///////////////////////////////     ORDENAR TARJETAS    //////////////////////////////
    @PostMapping("/orden")
    public ResponseEntity<GenericResponse> ordenTarjetas(HttpServletRequest request, @RequestBody ArrayList<TarjetaDto> tarjetasList) {
        GenericResponse response = new GenericResponse();

        try {

            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            response = tarjetasService.ordenTarjetas(spaceId, uid, tarjetasList);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al ordenar las tarjeta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        return ResponseEntity.ok(response);
    }

    ///////////////////////////////     ACTIVAR / DESACTIVAR TARJETA    //////////////////////////////
    @PutMapping("/activar/{tarjetaId}")
    public ResponseEntity<GenericResponse> activarTarjeta(HttpServletRequest request, @PathVariable String tarjetaId, @RequestBody boolean activa) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            if (tarjetaId == null || tarjetaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la tarjeta es obligatorio.");
                return ResponseEntity.ok(response);
            }

            response = tarjetasService.activarTarjeta(spaceId, uid, tarjetaId, activa);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al activar la tarjeta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}

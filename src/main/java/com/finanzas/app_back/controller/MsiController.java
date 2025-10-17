package com.finanzas.app_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Msi.MsiDto;
import com.finanzas.app_back.service.MsiService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/msi")
public class MsiController {

    @Autowired
    private MsiService MsiService;

    @PostMapping("/registrar")
    public ResponseEntity<GenericResponse> registrarMsi(HttpServletRequest request, @RequestBody MsiDto msiData) {
        try {
            String uid = (String) request.getAttribute("uid");

            System.out.println("Datos recibidos en el controlador: " + msiData);

            String valida = msiData.validaCampos();
            if (!valida.isEmpty()) {
                GenericResponse response = new GenericResponse();
                response.setCoderr("1002");
                response.setMessage(valida);
                return ResponseEntity.ok(response);
            }

            GenericResponse response = MsiService.registrarMsi(uid, msiData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al registrar los MSI");
        }
    }

     @GetMapping("/by-tarjeta/{tarjetaId}")
    public ResponseEntity<GenericResponse> getMsisByTarjetaId(HttpServletRequest request, @PathVariable String tarjetaId) {
        try {
            String uid = (String) request.getAttribute("uid");

            GenericResponse response = MsiService.obtenerMsisByTarjetaId(uid, tarjetaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al obtener los MSI");
        }
    }

    @GetMapping("")
    public ResponseEntity<GenericResponse> getMsis(HttpServletRequest request) {
        try {
            String uid = (String) request.getAttribute("uid");

            System.out.println("UID en getMsis: " + uid);

            GenericResponse response = MsiService.obtenerMsis(uid);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al obtener los MSI");
        }
    }

    @DeleteMapping("/eliminar/{msiId}")
    public ResponseEntity<GenericResponse> deleteMsi(HttpServletRequest request, @PathVariable String msiId) {
        try {
            String uid = (String) request.getAttribute("uid");
            MsiService.eliminarMsi(uid, msiId);
            GenericResponse response = new GenericResponse();
            response.setCoderr("0000");
            response.setMessage("MSI eliminado exitosamente.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al eliminar el MSI");
        }
    }

    @PutMapping("actualizar/{msiId}")
    public GenericResponse actualizarMsi(HttpServletRequest request, @PathVariable String msiId, @RequestBody MsiDto updatedMsiData) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = (String) request.getAttribute("uid");

            String valida = updatedMsiData.validaCampos();
            if (!valida.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage(valida);
                return response;
            }

            response = MsiService.actualizaMsi(uid, msiId, updatedMsiData);
            return response;
        } catch (Exception e) {
            response = new GenericResponse();
            response.setCoderr("9999");
            response.setMessage("Error al actualizar el MSI: " + e.getMessage());
            return response;
        }
    }

    


    private ResponseEntity<GenericResponse> manejarExcepcion(Exception e, String mensaje) {
        GenericResponse response = new GenericResponse();
        response.setCoderr("9999");
        response.setMessage(mensaje + ": " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    

}

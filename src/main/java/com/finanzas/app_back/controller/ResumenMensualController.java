package com.finanzas.app_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.service.ResumenMensualService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/resumen")
public class ResumenMensualController {

    @Autowired
    private ResumenMensualService resumenMensualService;

    @GetMapping("/mensual")
    public ResponseEntity<GenericResponse> getResumenMensual(
            HttpServletRequest request,
            @RequestParam int mes,
            @RequestParam int anio) {

        GenericResponse response = new GenericResponse();

        try {
            if (mes < 1 || mes > 12) {
                response.setCoderr("1002");
                response.setMessage("El mes debe ser un valor entre 1 y 12.");
                return ResponseEntity.ok(response);
            }

            if (anio < 2000 || anio > 2100) {
                response.setCoderr("1002");
                response.setMessage("El año proporcionado no es válido.");
                return ResponseEntity.ok(response);
            }

            String uid = (String) request.getAttribute("uid");
            String spaceId = request.getHeader("X-Space-Id");

            response = resumenMensualService.obtenerResumenMensual(spaceId, uid, mes, anio);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al obtener el resumen mensual: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

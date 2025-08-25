package com.finanzas.app_back.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.finanzas.app_back.service.CuentasService;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Cuentas.CuentaDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@RestController
@RequestMapping("/api/cuentas")
public class CuentasController {

    @Autowired
    private CuentasService cuentasService;

    private final Key jwtKey = new SecretKeySpec(
    Base64.getDecoder().decode("dAHNfXl8x/3oc5zEdUqy+oxUZgthsM13wzhv/WSVWew="), 
    SignatureAlgorithm.HS256.getJcaName());

    @PostMapping("/registrar")
    public ResponseEntity<GenericResponse> registrarCuenta(
            @RequestHeader("Authorization") String token,
            @RequestBody CuentaDto cuentaData) {

        GenericResponse response = new GenericResponse();

        try {
            // Validar el token
            if (token == null || !token.startsWith("Bearer ")) {
                response.setCoderr("1001");
                response.setMessage("Token no proporcionado o inválido.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String jwt = token.substring(7); // Eliminar el prefijo "Bearer "
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtKey)
                    .build()
                    .parseClaimsJws(jwt)
                    .getBody();
                    
            String userId = claims.getSubject(); // Obtener el ID del usuario del token
            

            // Validar los datos de la cuenta
            String nombre = (String) cuentaData.getNombre();
            String descripcion = (String) cuentaData.getDescripcion();
            String institucion = (String) cuentaData.getInstitucion();
            String efectivo = String.valueOf(cuentaData.isEfectivo());
            String periodicidad = (String) cuentaData.getPeriodicidad();
            String saldo = String.valueOf(cuentaData.getSaldo());
            String inversion = String.valueOf(cuentaData.isInversion());
            String tasa = String.valueOf(cuentaData.getTasa()); 


            if (nombre == null || descripcion == null || institucion == null || periodicidad == null || saldo == null || inversion == null || tasa == null || efectivo == null) {
                response.setCoderr("1002");
                response.setMessage("Todos los campos son obligatorios.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Llamar al servicio para registrar la cuenta
            response = cuentasService.registrarCuenta(cuentaData);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al registrar la cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

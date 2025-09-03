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
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/cuentas")
public class CuentasController {

    @Autowired
    private CuentasService cuentasService;

    private final Key jwtKey = new SecretKeySpec(
            Base64.getDecoder().decode("dAHNfXl8x/3oc5zEdUqy+oxUZgthsM13wzhv/WSVWew="),
            SignatureAlgorithm.HS256.getJcaName());

    public String checkToken(String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            return "";
        }
        String jwt = token.substring(7); // Eliminar el prefijo "Bearer "
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtKey)
                .build()
                .parseClaimsJws(jwt)
                .getBody();

        String uid = claims.get("uid", String.class);
        System.out.println("UID from token: " + uid);

        return uid;
    }

    @GetMapping("")
    public ResponseEntity<GenericResponse> getCuentas(
            @RequestHeader("Authorization") String token) {

        GenericResponse response = new GenericResponse();

        try {
            String uid = checkToken(token);

            if (uid.isEmpty()) {
                response.setCoderr("1001");
                response.setMessage("Token inválido o no proporcionado.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            response = cuentasService.obtenerCuentas(uid);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al recuperar las cuentas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }

    @PostMapping("/registrar")
    public ResponseEntity<GenericResponse> registrarCuenta(
            @RequestHeader("Authorization") String token,
            @RequestBody CuentaDto cuentaData) {

        GenericResponse response = new GenericResponse();

        try {
            String uid = checkToken(token);

            if (uid.isEmpty()) {
                response.setCoderr("1001");
                response.setMessage("Token inválido o no proporcionado.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Validar los datos de la cuenta
            String nombre = (String) cuentaData.getNombre();
            String descripcion = (String) cuentaData.getDescripcion();
            String institucion = (String) cuentaData.getInstitucion();
            String periodicidad = (String) cuentaData.getPeriodicidad();
            String saldo = String.valueOf(cuentaData.getSaldo());
            String inversion = String.valueOf(cuentaData.isInversion());
            String tasa = String.valueOf(cuentaData.getTasa());

            if (nombre == null || descripcion == null || institucion == null || periodicidad == null || saldo == null
                    || inversion == null || tasa == null) {
                response.setCoderr("1002");
                response.setMessage("Todos los campos son obligatorios.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Llamar al servicio para registrar la cuenta
            response = cuentasService.registrarCuenta(uid, cuentaData);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al registrar la cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/eliminar/{cuentaId}")
    public ResponseEntity<GenericResponse> eliminarCuenta(
            @RequestHeader("Authorization") String token,
            @PathVariable String cuentaId) {
        GenericResponse response = new GenericResponse();

        try {
            String uid = checkToken(token);
            if (uid.isEmpty()) {
                response.setCoderr("1001");
                response.setMessage("Token inválido o no proporcionado.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }


            if(cuentaId == null || cuentaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la cuenta es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            response = cuentasService.eliminarCuenta(uid, cuentaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al eliminar la cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/actualizar/{cuentaId}")
    public ResponseEntity<GenericResponse> actualizarCuenta(
            @RequestHeader("Authorization") String token,
            @PathVariable String cuentaId,
            @RequestBody CuentaDto updatedCuentaData) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = checkToken(token);
            if (uid.isEmpty()) {
                response.setCoderr("1001");
                response.setMessage("Token inválido o no proporcionado.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if(cuentaId == null || cuentaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la cuenta es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Validar los datos de la cuenta
            String nombre = (String) updatedCuentaData.getNombre();
            String descripcion = (String) updatedCuentaData.getDescripcion();
            String institucion = (String) updatedCuentaData.getInstitucion();
            String periodicidad = (String) updatedCuentaData.getPeriodicidad();
            String saldo = String.valueOf(updatedCuentaData.getSaldo());
            String inversion = String.valueOf(updatedCuentaData.isInversion());
            String tasa = String.valueOf(updatedCuentaData.getTasa());

            if (nombre == null || descripcion == null || institucion == null || periodicidad == null || saldo == null
                    || inversion == null || tasa == null ) {
                response.setCoderr("1002");
                response.setMessage("Todos los campos son obligatorios.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }




            response = cuentasService.actualizarCuenta(uid, cuentaId, updatedCuentaData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al actualizar la cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{cuentaId}")
    public ResponseEntity<GenericResponse> consultaCuenta(
            @RequestHeader("Authorization") String token,
            @PathVariable String cuentaId) {
        GenericResponse response = new GenericResponse();
        try {
            String uid = checkToken(token);
            if (uid.isEmpty()) {
                response.setCoderr("1001");
                response.setMessage("Token inválido o no proporcionado.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            if(cuentaId == null || cuentaId.isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("El ID de la cuenta es obligatorio.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }


            response = cuentasService.consultaCuenta(uid, cuentaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al consultar la cuenta: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

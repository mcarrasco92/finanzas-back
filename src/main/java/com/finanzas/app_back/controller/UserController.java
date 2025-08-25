package com.finanzas.app_back.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.finanzas.app_back.service.UserService;
import com.finanzas.app_back.dto.User.UserRegistration;
import com.finanzas.app_back.dto.GenericResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<GenericResponse> registerUser(@RequestBody UserRegistration userRegistration) {
        try {

            if (userRegistration.getName() == null || userRegistration.getEmail() == null
                    || userRegistration.getPassword() == null) {
                GenericResponse response = new GenericResponse();
                response.setCoderr("1001");
                response.setMessage("Es necesario indicar el nombre, email y contraseña del usuario.");
                return ResponseEntity.ok().body(response);
            }

            // Agregar validacion si los campos son vacios
            if (userRegistration.getName().isEmpty() || userRegistration.getEmail().isEmpty()
                    || userRegistration.getPassword().isEmpty()) {
                GenericResponse response = new GenericResponse();
                response.setCoderr("1001");
                response.setMessage("Los campos nombre, email y contraseña no pueden estar vacíos.");
                return ResponseEntity.ok().body(response);
            }

            GenericResponse response = userService.registerUser(userRegistration);
            return ResponseEntity.ok().body(response);

        } catch (Exception e) {

            GenericResponse response = new GenericResponse();

            response.setCoderr("9999");
            response.setMessage("Error al registrar al usuario: " + e.getMessage() + " CODERR: " + e.toString());

            return ResponseEntity.ok().body(response);
        }
    }

    @PostMapping("/create-token")
    public ResponseEntity<GenericResponse> createToken(@RequestBody String email) {
        try {
            return ResponseEntity.ok(userService.createToken(email));
        } catch (Exception e) {
            GenericResponse response = new GenericResponse();
            response.setCoderr("9999");
            response.setMessage("Error generar token: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<GenericResponse> validateToken(@RequestBody Map<String, String> request) {
        String firebaseToken = request.get("firebaseToken");

        if (firebaseToken == null || firebaseToken.isEmpty()) {
            GenericResponse response = new GenericResponse();
            response.setCoderr("1005");
            response.setMessage("El token de Firebase es requerido.");
            return ResponseEntity.ok().body(response);
        }

        GenericResponse response = userService.validateFirebaseToken(firebaseToken);
        return ResponseEntity.ok(response);
    }
}

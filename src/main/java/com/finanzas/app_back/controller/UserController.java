package com.finanzas.app_back.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.finanzas.app_back.service.UserService;
import com.finanzas.app_back.dto.User.UserLogin;
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

            if (userRegistration.getName() == null || userRegistration.getEmail() == null || userRegistration.getPassword() == null) {
                GenericResponse response = new GenericResponse();
                response.setCoderr("9999");
                response.setMessage("Es necesario indicar el nombre, email y contraseña del usuario.");
                return ResponseEntity.badRequest().body(response);
            }

            //Agregar validacion si los campos son vacios
            if (userRegistration.getName().isEmpty() || userRegistration.getEmail().isEmpty() || userRegistration.getPassword().isEmpty()) {
                GenericResponse response = new GenericResponse();
                response.setCoderr("9999");
                response.setMessage("Los campos nombre, email y contraseña no pueden estar vacíos.");
                return ResponseEntity.badRequest().body(response);
            }
            


            GenericResponse response = userService.registerUser(userRegistration);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GenericResponse response = new GenericResponse();

            response.setCoderr("9999");
            response.setMessage("Error al registrar al usuario: " + e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<GenericResponse> loginUser(@RequestBody UserLogin userLogin) {
        try {
            // Validar que los campos no sean nulos
            if (userLogin.getEmail() == null || userLogin.getPassword() == null) {
                GenericResponse response = new GenericResponse();
                response.setCoderr("9999");
                response.setMessage("Es necesario indicar el email y la contraseña.");
                return ResponseEntity.badRequest().body(response);
            }

            if (userLogin.getEmail().isEmpty() || userLogin.getPassword().isEmpty()) {
                GenericResponse response = new GenericResponse();
                response.setCoderr("9999");
                response.setMessage("Los campos nombre, email y contraseña no pueden estar vacíos.");
                return ResponseEntity.badRequest().body(response);
            }

            // Llamar al servicio para autenticar al usuario
            GenericResponse response = userService.loginUser(userLogin);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GenericResponse response = new GenericResponse();
            response.setCoderr("9999");
            response.setMessage("Error al iniciar sesión: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}

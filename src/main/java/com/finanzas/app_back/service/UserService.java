package com.finanzas.app_back.service;

import org.springframework.stereotype.Service;

import com.finanzas.app_back.dto.User.UserRegistration;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.User.UserLogin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

@Service
public class UserService {
    public GenericResponse registerUser(UserRegistration dto) throws Exception {
        GenericResponse resp = new GenericResponse();

        try {
            System.out.println("Registering user: " + dto.getEmail());

            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(dto.getEmail())
                .setPassword(dto.getPassword())
                .setDisplayName(dto.getName());

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);

            resp.setCoderr("0000");
            resp.setMessage("USUARIO CREADO CORRECTAMENTE");
        } catch (FirebaseAuthException e) {
            if ("ALREADY_EXISTS".equals(e.getErrorCode().toString())) {
                resp.setCoderr("1001");
                resp.setMessage("El correo electrónico ya está registrado.");
            } else {
                resp.setCoderr("9999");
                resp.setMessage("Error desconocido: " + e.getMessage() + " CODERR: " + e.getErrorCode());
            }
        }

        return resp;
    }

    public GenericResponse loginUser(UserLogin userLogin) throws Exception {
        GenericResponse resp = new GenericResponse();

        try {
            // Verificar las credenciales con Firebase
            String token = FirebaseAuth.getInstance()
                .createCustomToken(userLogin.getEmail());

            resp.setCoderr("0000");
            resp.setMessage("Inicio de sesión exitoso.");
            resp.setData(token); // Devuelve el token de autenticación
        } catch (FirebaseAuthException e) {
            if ("INVALID_PASSWORD".equals(e.getErrorCode().toString())) {
                resp.setCoderr("1002");
                resp.setMessage("La contraseña es incorrecta.");
            } else if ("EMAIL_NOT_FOUND".equals(e.getErrorCode().toString())) {
                resp.setCoderr("1003");
                resp.setMessage("El correo electrónico no está registrado.");
            } else {
                resp.setCoderr("9999");
                resp.setMessage("Error desconocido: " + e.getMessage());
            }
        }

        return resp;
    }
}

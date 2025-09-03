package com.finanzas.app_back.service;

import org.springframework.stereotype.Service;

import com.finanzas.app_back.dto.User.UserRegistration;
import com.finanzas.app_back.dto.GenericResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class UserService {

    private final Key jwtKey = new SecretKeySpec(
    Base64.getDecoder().decode("dAHNfXl8x/3oc5zEdUqy+oxUZgthsM13wzhv/WSVWew="), 
    SignatureAlgorithm.HS256.getJcaName());


    public GenericResponse validateFirebaseToken(String firebaseToken) {
        GenericResponse resp = new GenericResponse();

        try {
            // Validar el token de Firebase
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            // Generar un token JWT propio
            String jwtToken = generaJwtToken(email, uid);

            resp.setCoderr("0000");
            resp.setMessage("Token validado y JWT generado exitosamente.");
            resp.setData(jwtToken); // Devuelve el JWT generado
        } catch (FirebaseAuthException e) {
            resp.setCoderr("1004");
            resp.setMessage("Token de Firebase inválido: " + e.getMessage());
        } catch (Exception e) {
            resp.setCoderr("9999");
            resp.setMessage("Error desconocido: " + e.getMessage());
        }

        return resp;
    }



    public GenericResponse registerUser(UserRegistration dto) {
        GenericResponse resp = new GenericResponse();

        try {
            System.out.println("Registering user: " + dto.getEmail());

            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(dto.getEmail())
                .setPassword(dto.getPassword())
                .setDisplayName(dto.getName());

            FirebaseAuth.getInstance().createUser(request);

            resp.setCoderr("0000");
            resp.setMessage("Usuario creado exitosamente.");
        } catch (FirebaseAuthException e) {
            if ("ALREADY_EXISTS".equals(e.getErrorCode().toString())) {
                resp.setCoderr("1001");
                resp.setMessage("El correo electrónico ya está registrado.");
            } else {
                resp.setCoderr("9998");
                resp.setMessage("Error desconocido: " + e.getMessage());
                System.out.println("Coderr: " + e.getErrorCode().toString());
            }
        } catch (Exception e) {
            resp.setCoderr("9999");
            resp.setMessage("Error desconocido: " + e.getMessage());
            System.out.println("Exception: " + e.getMessage());
        }

        return resp;
    }


    public GenericResponse createToken(String email, String uid) {

        GenericResponse resp = new GenericResponse();

        try {

            String jwtToken = generaJwtToken(email, uid);

            resp.setCoderr("0000");
            resp.setMessage("Token validado y JWT generado exitosamente.");
            resp.setData(jwtToken); // Devuelve el JWT generado
        } catch (Exception e) {
            resp.setCoderr("9999");
            resp.setMessage("Error al generar el token.");
        }
        return resp;
    }

    private String generaJwtToken(String email, String uid) {

        System.out.println("Generating JWT for email: " + email + " and uid: " + uid);

        return Jwts.builder()
                .setSubject(uid)
                .setIssuer("tu-backend")
                .setIssuedAt(new Date())
                .claim("email", email)
                .claim("uid", uid)
                .signWith(jwtKey)
                .compact();
    }
}

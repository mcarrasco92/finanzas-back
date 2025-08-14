package com.finanzas.app_back.controller;

import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.User.UserLogin;
import com.finanzas.app_back.dto.User.UserRegistration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testRegisterUser_Success() {
        String url = "http://localhost:" + port + "/api/users/register";

        UserRegistration userRegistration = new UserRegistration();
        userRegistration.setName("Test User");
        userRegistration.setEmail("test@example.com");
        userRegistration.setPassword("password123");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<UserRegistration> request = new HttpEntity<>(userRegistration, headers);

        ResponseEntity<GenericResponse> response = restTemplate.exchange(url, HttpMethod.POST, request, GenericResponse.class);

        assertEquals(200, response.getStatusCode().value());
        //assertEquals("1001", response.getBody().getCoderr());
        //assertEquals("USUARIO CREADO CORRECTAMENTE", response.getBody().getMessage());
    }

    @Test
    public void testLoginUser_Success() {
        String url = "http://localhost:" + port + "/api/users/login";

        UserLogin userLogin = new UserLogin();
        userLogin.setEmail("test@example.com");
        userLogin.setPassword("password123");

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<UserLogin> request = new HttpEntity<>(userLogin, headers);

        ResponseEntity<GenericResponse> response = restTemplate.exchange(url, HttpMethod.POST, request, GenericResponse.class);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("0000", response.getBody().getCoderr());
        assertEquals("Inicio de sesión exitoso.", response.getBody().getMessage());
    }
}
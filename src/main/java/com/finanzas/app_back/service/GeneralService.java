package com.finanzas.app_back.service;

import org.springframework.stereotype.Service;

import com.finanzas.app_back.dto.GenericResponse;
@Service
public class GeneralService {

    public GenericResponse handleExcepcion(Exception e, String mensaje) {
        GenericResponse response = new GenericResponse();
        response.setCoderr("9999");
        response.setMessage(mensaje + ": " + e.getMessage());
        return response;
    }

}

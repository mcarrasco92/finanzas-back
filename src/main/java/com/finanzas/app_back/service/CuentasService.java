package com.finanzas.app_back.service;

import org.springframework.stereotype.Service;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Cuentas.CuentaDto;

@Service
public class CuentasService {

    public GenericResponse registrarCuenta(CuentaDto dto) {
        GenericResponse response = new GenericResponse();

        try {
            // Aquí puedes agregar la lógica para guardar la cuenta en la base de datos
            // Por ejemplo, usar un repositorio para persistir los datos

            response.setCoderr("0000");
            response.setMessage("Cuenta registrada exitosamente.");
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al registrar la cuenta: " + e.getMessage());
        }

        return response;
    }
}

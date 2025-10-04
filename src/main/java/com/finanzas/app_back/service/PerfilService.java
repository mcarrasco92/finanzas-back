package com.finanzas.app_back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Perfil.PerfilDto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;

@Service
public class PerfilService {

    @Autowired
    private GeneralService generalService;
    private GenericResponse response = new GenericResponse();

    public GenericResponse obtenerPerfil(String uid) {
        try {

            // Obtener el usuario por UID
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);

            
            PerfilDto perfil = new PerfilDto();
            perfil.setEmail(userRecord.getEmail());
            perfil.setDisplayName(userRecord.getDisplayName());
            // Extraer los datos del usuario

            response.setCoderr("0000");
            response.setMessage("Perfil obtenido exitosamente.");
            response.setData(perfil); // Reemplaza null con el objeto de perfil obtenido

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener el perfil");
        }

        return response;
    }
}

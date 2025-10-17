package com.finanzas.app_back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Msi.MsiDto;
import com.finanzas.app_back.model.Msi;
import com.finanzas.app_back.repositories.MsiRepository;

@Service
public class MsiService {

    @Autowired
    private GeneralService generalService;

    @Autowired
    private MsiRepository msiRepository;

    private GenericResponse response = new GenericResponse();

    public GenericResponse registrarMsi(String uid ,MsiDto dto) {

        try {

            Msi msi = new Msi();
            msi.setData(dto);

            System.out.println("MSI a registrar: " + msi);

            String msiId = msiRepository.newMsi(uid, msi);
            dto.setId(msiId);

            response.setCoderr("0000");
            response.setMessage("MSI registrados exitosamente.");
            response.setData(dto);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al registrar los MSI");
        }

        return response;
    }


    public GenericResponse obtenerMsisByTarjetaId(String uid, String tarjetaId) {
    
        try {

            response.setCoderr("0000");
            response.setMessage("MSI obtenidos exitosamente.");
            response.setData(msiRepository.getMsisByTarjetaId(uid, tarjetaId));

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener los MSI");
        }

        return response;
    }

    public GenericResponse obtenerMsis(String uid) {
    
        try {

            response.setCoderr("0000");
            response.setMessage("MSI obtenidos exitosamente.");

            System.out.println("UID en el servicio obtenerMsis: " + uid);
            response.setData(msiRepository.getMsis(uid));

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener los MSI");
        }

        return response;
    }

    public GenericResponse eliminarMsi(String uid, String msiId) {
    
        try {

            msiRepository.deleteMsi(uid, msiId);

            response.setCoderr("0000");
            response.setMessage("MSI eliminado exitosamente.");

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al eliminar el MSI");
        }

        return response;
    }

    public GenericResponse actualizaMsi(String uid, String msiId, MsiDto msiToUpdate) {

        try {

            Msi msi = new Msi();
            msi.setData(msiToUpdate);

            msiRepository.actualizarMsi(uid, msiId, msi);

            response.setCoderr("0000");
            response.setMessage("MSI actualizado exitosamente.");

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar el MSI");
        }

        return response;
    }

}

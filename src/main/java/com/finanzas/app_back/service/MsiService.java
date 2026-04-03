package com.finanzas.app_back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Msi.MsiDto;
import com.finanzas.app_back.model.Msi;
import com.finanzas.app_back.repositories.MsiRepository;
import com.finanzas.app_back.repositories.SpaceRepository;

@Service
public class MsiService {

    @Autowired
    private GeneralService generalService;

    @Autowired
    private MsiRepository msiRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    private GenericResponse response = new GenericResponse();

    public GenericResponse registrarMsi(String spaceId, String uid, MsiDto dto) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            Msi msi = new Msi();
            msi.setData(dto);

            System.out.println("MSI a registrar: " + msi);

            String msiId = msiRepository.newMsi(spaceId, msi);
            dto.setId(msiId);

            response.setCoderr("0000");
            response.setMessage("MSI registrados exitosamente.");
            response.setData(dto);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al registrar los MSI");
        }

        return response;
    }


    public GenericResponse obtenerMsisByTarjetaId(String spaceId, String uid, String tarjetaId) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            response.setCoderr("0000");
            response.setMessage("MSI obtenidos exitosamente.");
            response.setData(msiRepository.getMsisByTarjetaId(spaceId, tarjetaId));

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener los MSI");
        }

        return response;
    }

    public GenericResponse obtenerMsis(String spaceId, String uid) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            response.setCoderr("0000");
            response.setMessage("MSI obtenidos exitosamente.");

            System.out.println("SpaceId en el servicio obtenerMsis: " + spaceId);
            response.setData(msiRepository.getMsis(spaceId));

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener los MSI");
        }

        return response;
    }

    public GenericResponse obtenerMsiById(String spaceId, String uid, String msiId) {
        try {
            spaceRepository.validateMembership(spaceId, uid);
            MsiDto msi = msiRepository.getMsiById(spaceId, msiId);
            if (msi == null) {
                response.setCoderr("1004");
                response.setMessage("MSI no encontrado.");
                return response;
            }
            response.setCoderr("0000");
            response.setMessage("MSI obtenido exitosamente.");
            response.setData(msi);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener el MSI");
        }
        return response;
    }

    public GenericResponse eliminarMsi(String spaceId, String uid, String msiId) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            msiRepository.deleteMsi(spaceId, msiId);

            response.setCoderr("0000");
            response.setMessage("MSI eliminado exitosamente.");

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al eliminar el MSI");
        }

        return response;
    }

    public GenericResponse actualizaMsi(String spaceId, String uid, String msiId, MsiDto msiToUpdate) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            Msi msi = new Msi();
            msi.setData(msiToUpdate);

            msiRepository.actualizarMsi(spaceId, msiId, msi);

            response.setCoderr("0000");
            response.setMessage("MSI actualizado exitosamente.");

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar el MSI");
        }

        return response;
    }

}

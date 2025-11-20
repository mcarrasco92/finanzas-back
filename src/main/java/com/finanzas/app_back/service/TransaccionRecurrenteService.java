package com.finanzas.app_back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.TransaccionRecurrente.TransaccionRecurrenteDto;
import com.finanzas.app_back.model.TransaccionRecurrente;
import com.finanzas.app_back.repositories.TransaccionRecurrenteRepository;

@Service
public class TransaccionRecurrenteService {
    @Autowired
    private GeneralService generalService;
    @Autowired
    private TransaccionRecurrenteRepository repository;
    private GenericResponse response = new GenericResponse();

    public GenericResponse registrarTransaccionRecurrente(String uid, TransaccionRecurrenteDto dto) {
        try {
            TransaccionRecurrente transaccion = new TransaccionRecurrente();

            transaccion.setData(dto);

            String id = repository.newTransaccionRecurrente(uid, transaccion);
            dto.setId(id);
            response.setCoderr("0000");
            response.setMessage("Transacción recurrente registrada exitosamente.");
            response.setData(dto);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al registrar la transacción recurrente");
        }
        return response;
    }

    public GenericResponse obtenerTransaccionesRecurrentes(String uid) {
        try {
            response.setCoderr("0000");
            response.setMessage("Transacciones recurrentes obtenidas exitosamente.");
            response.setData(repository.getTransaccionesRecurrentes(uid));
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener las transacciones recurrentes");
        }
        return response;
    }

    public GenericResponse obtenerTransaccionRecurrenteById(String uid, String id) {
        try {
            response.setCoderr("0000");
            response.setMessage("Transacción recurrente obtenida exitosamente.");
            response.setData(repository.getTransaccionRecurrenteById(uid, id));
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener la transacción recurrente");
        }
        return response;
    }

    public GenericResponse eliminarTransaccionRecurrente(String uid, String id) {
        try {
            repository.deleteTransaccionRecurrente(uid, id);
            response.setCoderr("0000");
            response.setMessage("Transacción recurrente eliminada exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al eliminar la transacción recurrente");
        }
        return response;
    }

    public GenericResponse actualizarTransaccionRecurrente(String uid, String id, TransaccionRecurrenteDto dto) {
        try {
            TransaccionRecurrente transaccion = new TransaccionRecurrente();
            transaccion.setData(dto);
            repository.actualizarTransaccionRecurrente(uid, id, transaccion);
            response.setCoderr("0000");
            response.setMessage("Transacción recurrente actualizada exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar la transacción recurrente");
        }
        return response;
    }
}
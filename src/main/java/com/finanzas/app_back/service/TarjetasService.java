package com.finanzas.app_back.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Tarjetas.TarjetaDto;
import com.finanzas.app_back.dto.Tarjetas.TarjetasList;
import com.finanzas.app_back.model.Tarjeta;
import com.finanzas.app_back.repositories.TarjetasRepository;

@Service
public class TarjetasService {


    @Autowired
    private GeneralService generalService;

    @Autowired
    private TarjetasRepository tarjetasRepository;

    private GenericResponse response = new GenericResponse();


    public GenericResponse registrarTarjeta(String uid ,TarjetaDto dto) {

        try {

            Tarjeta tarjeta = new Tarjeta();
            tarjeta.setDataDto(dto);

            String tarjetaId = tarjetasRepository.newTarjeta(uid, tarjeta);
            dto.setId(tarjetaId);

            response.setCoderr("0000");
            response.setMessage("Tarjeta registrada exitosamente.");
            response.setData(dto);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al registrar la tarjeta");
        }

        return response;
    }


    public GenericResponse obtenerTarjetas(String uid) {
        
    
        try {

            ArrayList<TarjetaDto> tarjetas = tarjetasRepository.getTarjetas(uid);

            if (tarjetas.isEmpty()) {
                response.setCoderr("0001");
                response.setMessage("No se encontraron tarjetas.");
                return response;
            }

            TarjetasList tarjetasList = new TarjetasList();
            tarjetasList.setTarjetas(tarjetas);


            response.setCoderr("0000");
            response.setMessage("Tarjetas obtenidas exitosamente.");
            response.setData(tarjetasList);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener las tarjetas");
        }

        return response;
    }


    public GenericResponse consultaTarjeta(String uid, String tarjetaId) {

        try {

            TarjetaDto tarjeta = tarjetasRepository.getTarjetaById(uid, tarjetaId);

            if(tarjeta == null){
                response.setCoderr("0001");
                response.setMessage("Tarjeta no encontrada.");
                return response;
            }

            response.setCoderr("0000");
            response.setMessage("Tarjeta obtenida exitosamente.");
            response.setData(tarjeta);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener la tarjeta");
        }

        return response;
    }


    public GenericResponse actualizarTarjeta(String uid, String tarjetaId, TarjetaDto updatedTarjetaDto) {

        try {

            TarjetaDto existingTarjetaDto = tarjetasRepository.getTarjetaById(uid, tarjetaId);

            if(existingTarjetaDto == null){
                response.setCoderr("0001");
                response.setMessage("Tarjeta no encontrada.");
                return response;
            } 

            existingTarjetaDto.setNombre(updatedTarjetaDto.getNombre());
            existingTarjetaDto.setDescripcion(updatedTarjetaDto.getDescripcion());
            existingTarjetaDto.setInstitucion(updatedTarjetaDto.getInstitucion());
            existingTarjetaDto.setDpago(updatedTarjetaDto.getDpago());
            existingTarjetaDto.setDcorte(updatedTarjetaDto.getDcorte());


            Tarjeta tarjeta = new Tarjeta();
            tarjeta.setDataDto(existingTarjetaDto);

            tarjetasRepository.updateTarjeta(uid, tarjetaId, tarjeta);

            response.setCoderr("0000");
            response.setMessage("Tarjeta actualizada exitosamente.");

            
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar la tarjeta");
        }

        return response;
    }


    public GenericResponse eliminarTarjeta(String uid, String tarjetaId) {

        try {
            
            TarjetaDto tarjeta = tarjetasRepository.getTarjetaById(uid, tarjetaId);

            if(tarjeta == null){
                response.setCoderr("0001");
                response.setMessage("Tarjeta no encontrada.");
                return response;
            }

            if(tarjeta.isActiva()){
                response.setCoderr("0002");
                response.setMessage("No se puede eliminar una tarjeta activa.");
                return response;
            }   

            tarjetasRepository.deleteTarjeta(uid, tarjetaId);

            response.setCoderr("0000");
            response.setMessage("Tarjeta eliminada exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al eliminar la tarjeta");
        }

        return response;
    }
    

    public GenericResponse ordenTarjetas(String uid, ArrayList<TarjetaDto> tarjetas){

        int orden = 1;
        String tarjetaId = "";

        try {

        for (TarjetaDto tarjetaFor : tarjetas) {

            tarjetaId = tarjetaFor.getId();
            
            if (tarjetaId != null) { 
                TarjetaDto tarjetaDto = tarjetasRepository.getTarjetaById(uid, tarjetaId);

                if(tarjetaDto != null){
                    tarjetaDto.setOrden(orden);
                    orden++;

                    Tarjeta tarjetaToUpdate = new Tarjeta();
                    tarjetaToUpdate.setDataDto(tarjetaDto);
                    tarjetasRepository.updateTarjeta(uid, tarjetaId, tarjetaToUpdate);
                }
                
            }
        }

        response.setCoderr("0000");
        response.setMessage("Tarjetas ordenadas exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al ordenar las tarjetas");
        }

        return response;
    }


    public GenericResponse activarTarjeta(String uid, String tarjetaId, boolean activa) {

        try {


            TarjetaDto tarjetaDto = tarjetasRepository.getTarjetaById(uid, tarjetaId);

            if(tarjetaDto == null){
                response.setCoderr("0001");
                response.setMessage("Tarjeta no encontrada.");
                return response;
            }

            tarjetaDto.setActiva(activa);


            Tarjeta tarjetaToUpdate = new Tarjeta();
            tarjetaToUpdate.setDataDto(tarjetaDto);
            tarjetasRepository.updateTarjeta(uid, tarjetaId, tarjetaToUpdate);

            response.setCoderr("0000");
            response.setMessage("Tarjeta actualizada exitosamente.");
            response.setData(activa);

            
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar la tarjeta");
        }

        return response;
    }

}

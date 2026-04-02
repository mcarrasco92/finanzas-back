package com.finanzas.app_back.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Tarjetas.TarjetaDto;
import com.finanzas.app_back.dto.Tarjetas.TarjetasList;
import com.finanzas.app_back.dto.Transacciones.TransaccionDto;
import com.finanzas.app_back.model.Tarjeta;
import com.finanzas.app_back.repositories.SpaceRepository;
import com.finanzas.app_back.repositories.TarjetasRepository;
import com.finanzas.app_back.repositories.TransaccionesRepository;

@Service
public class TarjetasService {


    @Autowired
    private GeneralService generalService;

    @Autowired
    private TarjetasRepository tarjetasRepository;
    @Autowired
    private TransaccionesRepository transaccionesRepository;
    @Autowired
    private SpaceRepository spaceRepository;

    private GenericResponse response = new GenericResponse();


    public GenericResponse registrarTarjeta(String spaceId, String uid, TarjetaDto dto) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            Tarjeta tarjeta = new Tarjeta();
            tarjeta.setDataDto(dto);
            tarjeta.setSaldo(0.0);
            tarjeta.setActiva(true);

            String tarjetaId = tarjetasRepository.newTarjeta(spaceId, tarjeta);
            dto.setId(tarjetaId);

            response.setCoderr("0000");
            response.setMessage("Tarjeta registrada exitosamente.");
            response.setData(dto);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al registrar la tarjeta");
        }

        return response;
    }


    public GenericResponse obtenerTarjetas(String spaceId, String uid) {


        try {

            spaceRepository.validateMembership(spaceId, uid);

            ArrayList<TarjetaDto> tarjetas = tarjetasRepository.getTarjetas(spaceId);

            if (tarjetas.isEmpty()) {
                response.setCoderr("0001");
                response.setMessage("No se encontraron tarjetas.");
                return response;
            }

            TarjetasList tarjetasList = new TarjetasList();

            double saldoTotal = 0.0;
            double saldoMensual = 0.0;
            double saldoAPagar = 0.0;
            double saldoMsiFuturosIndividual = 0.0;

            LocalDate fechaActual = LocalDate.now();

            // Formatear la fecha en el formato "yyyy-MM"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            String fechaFormateada = fechaActual.format(formatter);

            ArrayList<TransaccionDto> transacciones = new ArrayList<>();

            for(TarjetaDto tarjeta: tarjetas){

                tarjeta.setSaldoPeriodoActual(tarjetasRepository.saldoPeriodoActual(spaceId, tarjeta.getId()));
                saldoMsiFuturosIndividual = tarjetasRepository.saldoMsiFuturos(spaceId, tarjeta.getId());
                tarjeta.setPagoPendiente(tarjeta.getSaldo() - tarjeta.getSaldoPeriodoActual() - saldoMsiFuturosIndividual);

                if(tarjeta.isActiva()){
                    saldoTotal += tarjeta.getSaldo();

                    transacciones = transaccionesRepository.getTransaccionesTarjetaByMonth(spaceId, fechaFormateada ,tarjeta.getId());

                    for(TransaccionDto transaccion: transacciones){

                        if(transaccion.getTipo().equals("Egreso")){
                            saldoMensual += transaccion.getImporte();
                        }
                    }

                    transacciones.clear();

                    saldoAPagar += tarjeta.getPagoPendiente();

                }


            }

            tarjetasList.setTarjetas(tarjetas);
            tarjetasList.setSaldoTotal(saldoTotal);
            tarjetasList.setSaldoMensual(saldoMensual);
            tarjetasList.setSaldoAPagar(saldoAPagar);


            response.setCoderr("0000");
            response.setMessage("Tarjetas obtenidas exitosamente.");
            response.setData(tarjetasList);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener las tarjetas");
        }

        return response;
    }


    public GenericResponse consultaTarjeta(String spaceId, String uid, String tarjetaId) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            TarjetaDto tarjeta = tarjetasRepository.getTarjetaById(spaceId, tarjetaId);

            if(tarjeta == null){
                response.setCoderr("0001");
                response.setMessage("Tarjeta no encontrada.");
                return response;
            }

            tarjeta.setTransacciones(transaccionesRepository.getExistTransaccionesByTarjeta(spaceId, tarjetaId));
            tarjeta.setPagoPendiente(tarjetasRepository.pagoPendiente(spaceId, tarjetaId));

            response.setCoderr("0000");
            response.setMessage("Tarjeta obtenida exitosamente.");
            response.setData(tarjeta);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener la tarjeta");
        }

        return response;
    }


    public GenericResponse actualizarTarjeta(String spaceId, String uid, String tarjetaId, TarjetaDto updatedTarjetaDto) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            TarjetaDto existingTarjetaDto = tarjetasRepository.getTarjetaById(spaceId, tarjetaId);

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

            tarjetasRepository.updateTarjeta(spaceId, tarjetaId, tarjeta);

            existingTarjetaDto.setTransacciones(transaccionesRepository.getExistTransaccionesByTarjeta(spaceId, tarjetaId));

            response.setCoderr("0000");
            response.setMessage("Tarjeta actualizada exitosamente.");
            response.setData(existingTarjetaDto);


        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar la tarjeta");
        }

        return response;
    }


    public GenericResponse eliminarTarjeta(String spaceId, String uid, String tarjetaId) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            TarjetaDto tarjeta = tarjetasRepository.getTarjetaById(spaceId, tarjetaId);

            if(tarjeta == null){
                response.setCoderr("0001");
                response.setMessage("Tarjeta no encontrada.");
                return response;
            }

            if(transaccionesRepository.getExistTransaccionesByTarjeta(spaceId, tarjetaId)){
                response.setCoderr("1003");
                response.setMessage("No se puede eliminar la tarjeta porque tiene transacciones asociadas.");
                return response;
            }

            tarjetasRepository.deleteTarjeta(spaceId, tarjetaId);

            response.setCoderr("0000");
            response.setMessage("Tarjeta eliminada exitosamente.");
            response.setData(null);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al eliminar la tarjeta");
        }

        return response;
    }


    public GenericResponse ordenTarjetas(String spaceId, String uid, ArrayList<TarjetaDto> tarjetas){

        int orden = 1;
        String tarjetaId = "";

        try {

            spaceRepository.validateMembership(spaceId, uid);

        for (TarjetaDto tarjetaFor : tarjetas) {

            tarjetaId = tarjetaFor.getId();

            if (tarjetaId != null) {
                TarjetaDto tarjetaDto = tarjetasRepository.getTarjetaById(spaceId, tarjetaId);

                if(tarjetaDto != null){
                    tarjetaDto.setOrden(orden);
                    orden++;

                    Tarjeta tarjetaToUpdate = new Tarjeta();
                    tarjetaToUpdate.setDataDto(tarjetaDto);
                    tarjetasRepository.updateTarjeta(spaceId, tarjetaId, tarjetaToUpdate);
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


    public GenericResponse activarTarjeta(String spaceId, String uid, String tarjetaId, boolean activa) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            TarjetaDto tarjetaDto = tarjetasRepository.getTarjetaById(spaceId, tarjetaId);

            if(tarjetaDto == null){
                response.setCoderr("0001");
                response.setMessage("Tarjeta no encontrada.");
                return response;
            }

            tarjetaDto.setActiva(activa);


            Tarjeta tarjetaToUpdate = new Tarjeta();
            tarjetaToUpdate.setDataDto(tarjetaDto);
            tarjetasRepository.updateTarjeta(spaceId, tarjetaId, tarjetaToUpdate);

            response.setCoderr("0000");
            response.setMessage("Tarjeta actualizada exitosamente.");
            response.setData(activa);


        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar la tarjeta");
        }

        return response;
    }

}

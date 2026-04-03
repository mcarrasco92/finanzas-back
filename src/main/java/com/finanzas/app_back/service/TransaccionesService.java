package com.finanzas.app_back.service;

import java.time.LocalDate;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Cuentas.CuentaDto;
import com.finanzas.app_back.dto.Tarjetas.TarjetaDto;
import com.finanzas.app_back.dto.Transacciones.TransaccionesList;
import com.finanzas.app_back.dto.Transacciones.TransaccionDto;
import com.finanzas.app_back.dto.Transacciones.TransaccionFiltroDto;
import com.finanzas.app_back.model.Cuenta;
import com.finanzas.app_back.model.Tarjeta;
import com.finanzas.app_back.model.Transaccion;
import com.finanzas.app_back.repositories.TransaccionesRepository;
import com.finanzas.app_back.repositories.CuentasRepository;
import com.finanzas.app_back.repositories.SpaceRepository;
import com.finanzas.app_back.repositories.TarjetasRepository;

@Service
public class TransaccionesService {

    @Autowired
    private GeneralService generalService;

    @Autowired
    private TransaccionesRepository transaccionesRepository;

    @Autowired
    private CuentasRepository cuentasRepository;

    @Autowired
    private TarjetasRepository tarjetasRepository;

    @Autowired
    private SpaceRepository spaceRepository;


    private GenericResponse response = new GenericResponse();

    public GenericResponse registrarTransaccion(String spaceId, String uid, TransaccionDto dto) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            Transaccion transaccion = new Transaccion();
            transaccion.setDataDto(dto);

            if(transaccion.getCuentaId() != null && !transaccion.getCuentaId().isEmpty()){
                String transaccionId = transaccionesRepository.newTransaccionCuenta(spaceId, transaccion);
                dto.setId(transaccionId);
            } else if(transaccion.getTarjetaId() != null && !transaccion.getTarjetaId().isEmpty()){
                String transaccionId = transaccionesRepository.newTransaccionTarjeta(spaceId, transaccion);
                dto.setId(transaccionId);
            } else {
                response.setCoderr("1003");
                response.setMessage("Debe especificar una cuenta o tarjeta para la transaccion.");
                return response;
            }

            response.setCoderr("0000");
            response.setMessage("Transaccion registrada exitosamente.");
            response.setData(dto);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al registrar la transaccion");
        }

        return response;
    }

    // Called internally by TransaccionRecurrenteRepository (no uid available)
    public GenericResponse registrarTransaccion(String spaceId, TransaccionDto dto) {
        try {
            Transaccion transaccion = new Transaccion();
            transaccion.setDataDto(dto);

            if(transaccion.getCuentaId() != null && !transaccion.getCuentaId().isEmpty()){
                String transaccionId = transaccionesRepository.newTransaccionCuenta(spaceId, transaccion);
                dto.setId(transaccionId);
            } else if(transaccion.getTarjetaId() != null && !transaccion.getTarjetaId().isEmpty()){
                String transaccionId = transaccionesRepository.newTransaccionTarjeta(spaceId, transaccion);
                dto.setId(transaccionId);
            } else {
                response.setCoderr("1003");
                response.setMessage("Debe especificar una cuenta o tarjeta para la transaccion.");
                return response;
            }

            response.setCoderr("0000");
            response.setMessage("Transaccion registrada exitosamente.");
            response.setData(dto);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al registrar la transaccion");
        }
        return response;
    }




    public GenericResponse obtenerTransacciones(String spaceId, String uid, TransaccionFiltroDto filtroDto) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            if(filtroDto.getCuentaId() != null && !filtroDto.getCuentaId().isEmpty() && filtroDto.getTarjetaId() != null && !filtroDto.getTarjetaId().isEmpty()) {
                response.setCoderr("1002");
                response.setMessage("No puede filtrar por cuenta y tarjeta al mismo tiempo.");
                return response;
            }

            ArrayList<TransaccionDto> transacciones = new ArrayList<>();

            if(filtroDto.getCuentaId() != null && !filtroDto.getCuentaId().isEmpty()) {

                if(filtroDto.getYearMonth() == null || filtroDto.getYearMonth().isEmpty()) {
                    response.setCoderr("1002");
                    response.setMessage("El campo 'yearMonth' es obligatorio.");
                    return response;
                }


                transacciones = transaccionesRepository.getTransaccionesCuentaByMonth(spaceId, filtroDto.getYearMonth(), filtroDto.getCuentaId());
            } else if(filtroDto.getTarjetaId() != null && !filtroDto.getTarjetaId().isEmpty()) {

                if(filtroDto.getFechaInicio() == null || filtroDto.getFechaInicio().isEmpty() || filtroDto.getFechaFin() == null || filtroDto.getFechaFin().isEmpty()) {
                    response.setCoderr("1002");
                    response.setMessage("Los campos 'fechaInicio' y 'fechaFin' son obligatorios.");
                    return response;
                }
                transacciones = transaccionesRepository.getTransaccionesTarjetaByCut(spaceId, filtroDto.getFechaInicio(), filtroDto.getFechaFin(), filtroDto.getTarjetaId());
            }else{
                response.setCoderr("0001");
                response.setMessage("No se informo cuenta o tarjeta para filtrar.");
                return response;
            }

            if (transacciones.isEmpty()) {
                response.setCoderr("0001");
                response.setMessage("No se encontraron transacciones.");
                response.setData(null);
                return response;
            }


            TransaccionesList transaccionesList = new TransaccionesList();
            transaccionesList.setTransacciones(transacciones);

            response.setCoderr("0000");
            response.setMessage("Transacciones obtenidas exitosamente.");
            response.setData(transaccionesList);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener las transacciones");
        }

        return response;
    }


    public GenericResponse consultaTransaccion(String spaceId, String uid, String transaccionId) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            TransaccionDto transaccion = transaccionesRepository.getTransaccionById(spaceId, transaccionId);

            if(transaccion == null){
                response.setCoderr("0001");
                response.setMessage("Transaccion no encontrada.");
                return response;
            }

            response.setCoderr("0000");
            response.setMessage("Transaccion obtenida exitosamente.");
            response.setData(transaccion);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener la transaccion");
        }

        return response;
    }


    public GenericResponse eliminarTransaccion(String spaceId, String uid, String transaccionId) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            TransaccionDto transaccion = transaccionesRepository.getTransaccionById(spaceId, transaccionId);

            if(transaccion == null){
                response.setCoderr("0001");
                response.setMessage("Transaccion no encontrada.");
                return response;
            }

            transaccionesRepository.deleteTransaccion(spaceId, transaccionId);

            response.setCoderr("0000");
            response.setMessage("Transaccion eliminada exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al eliminar la transaccion");
        }

        return response;
    }



    public GenericResponse actualizarTransaccion(String spaceId, String uid, String transaccionId, TransaccionDto updatedTransaccionDto) {

        try {

            spaceRepository.validateMembership(spaceId, uid);

            TipoActualizacion tipoActualizacion = indetificaActualizacion(spaceId, transaccionId, updatedTransaccionDto);

            String nuevoId = "";
            Transaccion transaccion = new Transaccion();
            transaccion.setDataDto(updatedTransaccionDto);

            TransaccionDto transaccionOriginal = transaccionesRepository.getTransaccionById(spaceId, transaccionId);

            System.out.println("Tipo de actualizacion identificado: " + tipoActualizacion);

            switch (tipoActualizacion) {
                case DIFERENTE_CUENTA:
                    transaccionesRepository.deleteTransaccion(spaceId, transaccionId);
                    nuevoId = transaccionesRepository.newTransaccionCuenta(spaceId, transaccion);
                    updatedTransaccionDto.setId(nuevoId);
                    break;

                case DIFERENTE_TARJETA:
                    transaccionesRepository.deleteTransaccion(spaceId, transaccionId);
                    nuevoId = transaccionesRepository.newTransaccionTarjeta(spaceId, transaccion);
                    updatedTransaccionDto.setId(nuevoId);
                    break;

                case DIFERENTE_MONTO_MISMA_CUENTA:

                    if(transaccionOriginal.getCuentaId() != null && !transaccionOriginal.getCuentaId().isEmpty()){

                        CuentaDto cuentaDto = cuentasRepository.getCuentaById(spaceId, transaccionOriginal.getCuentaId());

                        if(transaccionOriginal.getImporte() > updatedTransaccionDto.getImporte()){

                            if(transaccionOriginal.getTipo().equalsIgnoreCase("ingreso")){
                                cuentaDto.setSaldo(cuentaDto.getSaldo() - (transaccionOriginal.getImporte() - updatedTransaccionDto.getImporte()));
                            } else {
                                cuentaDto.setSaldo(cuentaDto.getSaldo() + (transaccionOriginal.getImporte() - updatedTransaccionDto.getImporte()));
                            }

                        } else if(transaccionOriginal.getImporte() < updatedTransaccionDto.getImporte()){

                            if(transaccionOriginal.getTipo().equalsIgnoreCase("ingreso")){
                                cuentaDto.setSaldo(cuentaDto.getSaldo() + (updatedTransaccionDto.getImporte() - transaccionOriginal.getImporte()));
                            } else {
                                cuentaDto.setSaldo(cuentaDto.getSaldo() - (updatedTransaccionDto.getImporte() - transaccionOriginal.getImporte()));
                            }

                        }else{
                            response.setCoderr("1004");
                            response.setMessage("Ocurrio un error en el calculo del importe.");
                            return response;
                        }

                        Cuenta cuenta = new Cuenta();
                        cuenta.setDataDto(cuentaDto);

                        cuentasRepository.updateCuenta(spaceId, transaccionOriginal.getCuentaId(), cuenta);

                        Transaccion transaccionToUpdate = new Transaccion();
                        transaccionToUpdate.setDataDto(updatedTransaccionDto);

                        transaccionesRepository.updateTransaccion(spaceId, updatedTransaccionDto.getId(), transaccionToUpdate);

                    } else if(transaccionOriginal.getTarjetaId() != null && !transaccionOriginal.getTarjetaId().isEmpty()){

                        TarjetaDto tarjetaDto = tarjetasRepository.getTarjetaById(spaceId, transaccionOriginal.getTarjetaId());

                        if(transaccionOriginal.getImporte() > updatedTransaccionDto.getImporte()){

                            if(transaccionOriginal.getTipo().equalsIgnoreCase("ingreso")){
                                tarjetaDto.setSaldo(tarjetaDto.getSaldo() + (transaccionOriginal.getImporte() - updatedTransaccionDto.getImporte()));
                            } else {
                                tarjetaDto.setSaldo(tarjetaDto.getSaldo() - (transaccionOriginal.getImporte() - updatedTransaccionDto.getImporte()));
                            }

                        } else if(transaccionOriginal.getImporte() < updatedTransaccionDto.getImporte()){

                            if(transaccionOriginal.getTipo().equalsIgnoreCase("ingreso")){
                                tarjetaDto.setSaldo(tarjetaDto.getSaldo() - (updatedTransaccionDto.getImporte() - transaccionOriginal.getImporte()));
                            } else {
                                tarjetaDto.setSaldo(tarjetaDto.getSaldo() + (updatedTransaccionDto.getImporte() - transaccionOriginal.getImporte()));
                            }

                        }else{
                            response.setCoderr("1004");
                            response.setMessage("Ocurrio un error en el calculo del importe.");
                            return response;
                        }

                        Tarjeta tarjeta = new Tarjeta();
                        tarjeta.setDataDto(tarjetaDto);
                        tarjeta.setSaldo(tarjetaDto.getSaldo());

                        tarjetasRepository.updateTarjeta(spaceId, transaccionOriginal.getTarjetaId(), tarjeta);

                        Transaccion transaccionToUpdate = new Transaccion();
                        transaccionToUpdate.setDataDto(updatedTransaccionDto);


                        transaccionesRepository.updateTransaccion(spaceId, updatedTransaccionDto.getId(), transaccionToUpdate);

                    } else {
                        response.setCoderr("1003");
                        response.setMessage("Debe especificar una cuenta o tarjeta para la transaccion.");
                        return response;
                    }

                    break;

                case DEFAULT:

                    Transaccion transaccionToUpdate = new Transaccion();
                    transaccionToUpdate.setDataDto(updatedTransaccionDto);

                    transaccionesRepository.updateTransaccion(spaceId, updatedTransaccionDto.getId(), transaccionToUpdate);
                    break;

                case ERROR:

                    response.setCoderr("1003");
                    response.setMessage("Error al identificar el tipo de actualización.");
                    break;

                default:

                    response.setCoderr("1004");
                    response.setMessage("Ocurrio un error al identificar la actualizacion.");
                    break;
            }




            response.setCoderr("0000");
            response.setMessage("Transaccion actualizada exitosamente.");
            response.setData(updatedTransaccionDto);


        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar la transaccion");
        }

        return response;
    }


    private TipoActualizacion indetificaActualizacion(String spaceId, String TransaccionId, TransaccionDto dto){


        try {

            TransaccionDto transaccionOriginal = transaccionesRepository.getTransaccionById(spaceId, TransaccionId);

            if(dto.getCuentaId() != null && !dto.getCuentaId().isEmpty()){
                if(!transaccionOriginal.getCuentaId().equals(dto.getCuentaId())){
                    return TipoActualizacion.DIFERENTE_CUENTA;
                }
            } else if(dto.getTarjetaId() != null && !dto.getTarjetaId().isEmpty()){
                if(!transaccionOriginal.getTarjetaId().equals(dto.getTarjetaId())){
                    return TipoActualizacion.DIFERENTE_TARJETA;
                }
            }



            if (!transaccionOriginal.getImporte().equals(dto.getImporte())) {
                System.out.println("Se detecto un cambio en el importe de la transaccion" + transaccionOriginal.getImporte() + " a " + dto.getImporte());
                return TipoActualizacion.DIFERENTE_MONTO_MISMA_CUENTA;
            }

            return TipoActualizacion.DEFAULT;

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al identificar la actualizacion");
            return TipoActualizacion.ERROR;
        }


    }

    public GenericResponse obtenerTransaccionesParaBusqueda(String spaceId, String uid) {
        GenericResponse response = new GenericResponse();
        try {
            spaceRepository.validateMembership(spaceId, uid);

            LocalDate hoy = LocalDate.now();
            String fechaFin = hoy.toString();
            String fechaInicio = hoy.minusYears(1).toString();

            ArrayList<TransaccionDto> transacciones =
                    transaccionesRepository.getTransaccionesByDateRange(spaceId, fechaInicio, fechaFin);

            transacciones.sort((a, b) -> {
                if (a.getFecha() == null) return 1;
                if (b.getFecha() == null) return -1;
                return b.getFecha().compareTo(a.getFecha());
            });

            response.setCoderr("0000");
            response.setMessage("Transacciones obtenidas exitosamente.");
            response.setData(transacciones);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener transacciones para búsqueda");
        }
        return response;
    }

    private enum TipoActualizacion {
        DIFERENTE_CUENTA,
        DIFERENTE_TARJETA,
        DIFERENTE_MONTO_MISMA_CUENTA,
        DEFAULT,
        ERROR
    }




}

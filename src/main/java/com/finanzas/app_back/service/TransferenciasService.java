package com.finanzas.app_back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Cuentas.CuentaDto;
import com.finanzas.app_back.dto.Tarjetas.TarjetaDto;
import com.finanzas.app_back.dto.Transferencias.TransferenciaDto;
import com.finanzas.app_back.model.Transferencia;
import com.finanzas.app_back.repositories.CuentasRepository;
import com.finanzas.app_back.repositories.TarjetasRepository;
import com.finanzas.app_back.repositories.TransferenciasRepository;

@Service
public class TransferenciasService {

    @Autowired
    private TransferenciasRepository transferenciasRepository;

    @Autowired
    private CuentasRepository cuentasRepository;

    @Autowired
    private TarjetasRepository tarjetasRepository;

    @Autowired
    private GeneralService generalService;

    private GenericResponse response = new GenericResponse();

    public GenericResponse registrarTransferencia(String uid ,TransferenciaDto dto) {

        try {

            Transferencia transferencia = new Transferencia();
            transferencia.setDataDto(dto);

            if(!transferencia.getTipoCuentaDestino().equalsIgnoreCase("Cuenta") || transferencia.getTipoCuentaDestino().equalsIgnoreCase("Tarjeta")){
                response.setCoderr("1003");
                response.setMessage("El tipo de cuenta destino debe ser 'Cuenta' o 'Tarjeta'.");
                return response;
            }

            if(transferencia.getCuentaOrigenId().isEmpty() || transferencia.getCuentaDestinoId().isEmpty()){
                response.setCoderr("1004");
                response.setMessage("Debe especificar la cuenta origen y la cuenta destino.");
                return response;
            }

            if(transferencia.getCuentaOrigenId().equals(transferencia.getCuentaDestinoId())){
                response.setCoderr("1005");
                response.setMessage("La cuenta origen y la cuenta destino no pueden ser la misma.");
                return response;
            }

            CuentaDto cuentaOrigen = cuentasRepository.getCuentaById(uid, transferencia.getCuentaOrigenId());
            CuentaDto cuentaDestinoDto;
            TarjetaDto tarjetaDestinoDto;
            String transferenciaid = "";


            if(cuentaOrigen == null){
                response.setCoderr("1004");
                response.setMessage("La cuenta origen no existe.");
                return response;
            }
            
            if(transferencia.getTipoCuentaDestino().equalsIgnoreCase("Cuenta")){
                cuentaDestinoDto = cuentasRepository.getCuentaById(uid, transferencia.getCuentaDestinoId());
                if(cuentaDestinoDto == null){
                    response.setCoderr("1005");
                    response.setMessage("La cuenta destino no existe.");
                    return response;
                }

                transferenciaid = transferenciasRepository.newTransferenciaCuenta(uid, transferencia, cuentaOrigen, cuentaDestinoDto);


            } else if(transferencia.getTipoCuentaDestino().equalsIgnoreCase("Tarjeta")){
                tarjetaDestinoDto = tarjetasRepository.getTarjetaById(uid, transferencia.getCuentaDestinoId());
                if(tarjetaDestinoDto == null){
                    response.setCoderr("1006");
                    response.setMessage("La tarjeta destino no existe.");
                    return response;
                }

                transferenciaid = transferenciasRepository.newTransferenciaTarjeta(uid, transferencia, cuentaOrigen, tarjetaDestinoDto);
            }

            dto.setId(transferenciaid);

            response.setCoderr("0000");
            response.setMessage("Transferencia registrada exitosamente.");
            response.setData(dto);


        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al registrar la transaccion");
        }

        return response;
    }

    public GenericResponse getTransferenciaById(String uid, String transferenciaId) {
        try {
            TransferenciaDto transferencia = transferenciasRepository.getTransferenciaById(uid, transferenciaId);
            if (transferencia != null) {
                response.setCoderr("0000");
                response.setMessage("Transferencia obtenida exitosamente.");
                response.setData(transferencia);
            } else {
                response.setCoderr("1001");
                response.setMessage("No se encontró ninguna transferencia con el ID proporcionado.");
            }
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener la transferencia");
        }
        return response;
    }

    public GenericResponse eliminarTransferencia(String uid, String transferenciaId) {
        try {
            transferenciasRepository.deleteTransferencia(uid, transferenciaId);
            response.setCoderr("0000");
            response.setMessage("Transferencia eliminada exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al eliminar la transferencia");
        }
        return response;
    }

    public GenericResponse actualizarTransferencia(String uid, String transferenciaId, TransferenciaDto updatedData) {
        try {
            TransferenciaDto transferenciaDto = transferenciasRepository.updateTransferencia(uid, transferenciaId, updatedData);
            response.setCoderr("0000");
            response.setMessage("Transferencia actualizada exitosamente.");
            response.setData(transferenciaDto);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar la transferencia");
        }
        return response;
    }

}

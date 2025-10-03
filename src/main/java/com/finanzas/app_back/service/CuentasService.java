package com.finanzas.app_back.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Cuentas.CuentaDto;
import com.finanzas.app_back.dto.Cuentas.CuentasList;
import com.finanzas.app_back.model.Cuenta;

import com.finanzas.app_back.repositories.CuentasRepository;
import com.finanzas.app_back.repositories.TransaccionesRepository;




@Service
public class CuentasService {

    @Autowired
    private GeneralService generalService;

    @Autowired
    private CuentasRepository cuentasRepository;
    @Autowired
    private TransaccionesRepository transaccionesRepository;

    private GenericResponse response = new GenericResponse();


    public GenericResponse registrarCuenta(String uid ,CuentaDto dto) {

        try {

            Cuenta cuenta = new Cuenta();
            cuenta.setDataDto(dto);
            cuenta.setSaldo(0.0);
            cuenta.setActiva(true);

            String cuentaId = cuentasRepository.newCuenta(uid, cuenta);
            dto.setId(cuentaId);

            response.setCoderr("0000");
            response.setMessage("Cuenta registrada exitosamente.");
            response.setData(dto);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al registrar la cuenta");
        }

        return response;
    }


    public GenericResponse obtenerCuentas(String uid) {
        
        final Double[] saldoInvertido = {0.0};
        final Double[] saldoDisponible = {0.0};
        final Double[] saldoTotal = {0.0};
    
        try {

            ArrayList<CuentaDto> cuentas = cuentasRepository.getCuentas(uid);

            if (cuentas.isEmpty()) {
                response.setCoderr("0001");
                response.setMessage("No se encontraron cuentas.");
                return response;
            }
            
            for (CuentaDto cuenta : cuentas) {

                if(cuenta.isActiva()){
                    if(cuenta.isInversion()){
                        saldoInvertido[0] += cuenta.getSaldo();
                    }
        
                    if(cuenta.isVista()){
                        saldoDisponible[0] += cuenta.getSaldo();
                    } 
    
                    saldoTotal[0] += cuenta.getSaldo();
                }
                
            }

            CuentasList cuentasList = new CuentasList();
            cuentasList.setCuentas(cuentas);
            cuentasList.setSaldoDisponible(saldoDisponible[0]);
            cuentasList.setSaldoInvertido(saldoInvertido[0]);
            cuentasList.setSaldoTotal(saldoTotal[0]);  


            response.setCoderr("0000");
            response.setMessage("Cuentas obtenidas exitosamente.");
            response.setData(cuentasList);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener las cuentas");
        }

        return response;
    }


    public GenericResponse consultaCuenta(String uid, String cuentaId) {

        try {

            CuentaDto cuenta = cuentasRepository.getCuentaById(uid, cuentaId);

            if(cuenta == null){
                response.setCoderr("0001");
                response.setMessage("Cuenta no encontrada.");
                return response;
            }

            cuenta.setTransacciones(transaccionesRepository.getExistTransaccionesByCuenta(uid, cuentaId));

            response.setCoderr("0000");
            response.setMessage("Cuenta obtenida exitosamente.");
            response.setData(cuenta);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener la cuenta");
        }

        return response;
    }


    public GenericResponse actualizarCuenta(String uid, String cuentaId, CuentaDto updatedCuentaDto) {

        try {

            CuentaDto existingCuentaDto = cuentasRepository.getCuentaById(uid, cuentaId);

            if(existingCuentaDto == null){
                response.setCoderr("0001");
                response.setMessage("Cuenta no encontrada.");
                return response;
            } 

            existingCuentaDto.setNombre(updatedCuentaDto.getNombre());
            existingCuentaDto.setDescripcion(updatedCuentaDto.getDescripcion());
            existingCuentaDto.setInstitucion(updatedCuentaDto.getInstitucion());
            existingCuentaDto.setInversion(updatedCuentaDto.isInversion());
            existingCuentaDto.setVista(updatedCuentaDto.isVista());


            Cuenta cuenta = new Cuenta();
            cuenta.setDataDto(existingCuentaDto);

            cuentasRepository.updateCuenta(uid, cuentaId, cuenta);

            response.setCoderr("0000");
            response.setMessage("Cuenta actualizada exitosamente.");
            response.setData(existingCuentaDto);

            
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar la cuenta");
        }

        return response;
    }


    public GenericResponse eliminarCuenta(String uid, String cuentaId) {

        try {
            
            CuentaDto cuenta = cuentasRepository.getCuentaById(uid, cuentaId);

            if(cuenta == null){
                response.setCoderr("0001");
                response.setMessage("Cuenta no encontrada.");
                return response;
            }

            if(transaccionesRepository.getExistTransaccionesByCuenta(uid, cuentaId)){
                response.setCoderr("1003");
                response.setMessage("No se puede eliminar la cuenta porque tiene transacciones asociadas.");
                return response;
            }   

            cuentasRepository.deleteCuenta(uid, cuentaId);

            response.setCoderr("0000");
            response.setMessage("Cuenta eliminada exitosamente.");
            response.setData(null);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al eliminar la cuenta");
        }

        return response;
    }
    

    public GenericResponse ordenCuentas(String uid, ArrayList<CuentaDto> cuentas){

        int orden = 1;
        String cuentaId = "";

        try {

        for (CuentaDto cuentaFor : cuentas) {

            cuentaId = cuentaFor.getId();
            
            if (cuentaId != null) { 
                CuentaDto cuentaDto = cuentasRepository.getCuentaById(uid, cuentaId);

                if(cuentaDto != null){
                    cuentaDto.setOrden(orden);
                    orden++;

                    Cuenta cuentaToUpdate = new Cuenta();
                    cuentaToUpdate.setDataDto(cuentaDto);
                    cuentasRepository.updateCuenta(uid, cuentaId, cuentaToUpdate);
                }
                
            }
        }

        response.setCoderr("0000");
        response.setMessage("Cuentas ordenadas exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al ordenar las cuentas");
        }

        return response;
    }


    public GenericResponse activarCuenta(String uid, String cuentaId, boolean activa) {

        try {


            CuentaDto cuentaDto = cuentasRepository.getCuentaById(uid, cuentaId);

            if(cuentaDto == null){
                response.setCoderr("0001");
                response.setMessage("Cuenta no encontrada.");
                return response;
            }

            cuentaDto.setActiva(activa);


            Cuenta cuentaToUpdate = new Cuenta();
            cuentaToUpdate.setDataDto(cuentaDto);
            cuentasRepository.updateCuenta(uid, cuentaId, cuentaToUpdate);

            response.setCoderr("0000");
            response.setMessage("Cuenta actualizada exitosamente.");
            response.setData(activa);

            
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al actualizar la cuenta");
        }

        return response;
    }

}

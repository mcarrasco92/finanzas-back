package com.finanzas.app_back.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finanzas.app_back.dto.Categorias.CategoriaDto;
import com.finanzas.app_back.dto.Cuentas.CuentaDto;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Resumen.CuentaResumenDto;
import com.finanzas.app_back.dto.Resumen.ResumenMensualDto;
import com.finanzas.app_back.dto.Resumen.TarjetaResumenDto;
import com.finanzas.app_back.dto.Resumen.TransaccionConCategoriaDto;
import com.finanzas.app_back.dto.Tarjetas.TarjetaDto;
import com.finanzas.app_back.dto.Transacciones.TransaccionDto;
import com.finanzas.app_back.repositories.CategoriasRepository;
import com.finanzas.app_back.repositories.CuentasRepository;
import com.finanzas.app_back.repositories.TarjetasRepository;
import com.finanzas.app_back.repositories.TransaccionesRepository;

@Service
public class ResumenMensualService {

    @Autowired
    private GeneralService generalService;

    @Autowired
    private CuentasRepository cuentasRepository;

    @Autowired
    private TarjetasRepository tarjetasRepository;

    @Autowired
    private TransaccionesRepository transaccionesRepository;

    @Autowired
    private CategoriasRepository categoriasRepository;

    private GenericResponse response = new GenericResponse();

    public GenericResponse obtenerResumenMensual(String uid, int mes, int anio) {

        try {

            String yearMonth = String.format("%04d-%02d", anio, mes);

            // ---- Cuentas ----
            ArrayList<CuentaDto> cuentas = cuentasRepository.getCuentas(uid);
            List<CuentaResumenDto> cuentasResumen = new ArrayList<>();

            for (CuentaDto cuenta : cuentas) {
                ArrayList<TransaccionDto> transacciones =
                        transaccionesRepository.getTransaccionesCuentaByMonth(uid, yearMonth, cuenta.getId());

                List<TransaccionConCategoriaDto> transaccionesConCategoria =
                        enriquecerConCategoria(uid, transacciones);

                CuentaResumenDto cuentaResumen = new CuentaResumenDto();
                cuentaResumen.setId(cuenta.getId());
                cuentaResumen.setNombre(cuenta.getNombre());
                cuentaResumen.setDescripcion(cuenta.getDescripcion());
                cuentaResumen.setInstitucion(cuenta.getInstitucion());
                cuentaResumen.setSaldo(cuenta.getSaldo());
                cuentaResumen.setInversion(cuenta.isInversion());
                cuentaResumen.setVista(cuenta.isVista());
                cuentaResumen.setActiva(cuenta.isActiva());
                cuentaResumen.setOrden(cuenta.getOrden());
                cuentaResumen.setTransacciones(transaccionesConCategoria);

                cuentasResumen.add(cuentaResumen);
            }

            // ---- Tarjetas ----
            ArrayList<TarjetaDto> tarjetas = tarjetasRepository.getTarjetas(uid);
            List<TarjetaResumenDto> tarjetasResumen = new ArrayList<>();

            for (TarjetaDto tarjeta : tarjetas) {
                ArrayList<TransaccionDto> transacciones =
                        transaccionesRepository.getTransaccionesTarjetaByMonth(uid, yearMonth, tarjeta.getId());

                List<TransaccionConCategoriaDto> transaccionesConCategoria =
                        enriquecerConCategoria(uid, transacciones);

                TarjetaResumenDto tarjetaResumen = new TarjetaResumenDto();
                tarjetaResumen.setId(tarjeta.getId());
                tarjetaResumen.setNombre(tarjeta.getNombre());
                tarjetaResumen.setDescripcion(tarjeta.getDescripcion());
                tarjetaResumen.setInstitucion(tarjeta.getInstitucion());
                tarjetaResumen.setSaldo(tarjeta.getSaldo());
                tarjetaResumen.setDpago(tarjeta.getDpago());
                tarjetaResumen.setDcorte(tarjeta.getDcorte());
                tarjetaResumen.setActiva(tarjeta.isActiva());
                tarjetaResumen.setOrden(tarjeta.getOrden());
                tarjetaResumen.setTransacciones(transaccionesConCategoria);

                tarjetasResumen.add(tarjetaResumen);
            }

            ResumenMensualDto resumen = new ResumenMensualDto();
            resumen.setMes(mes);
            resumen.setAnio(anio);
            resumen.setCuentas(cuentasResumen);
            resumen.setTarjetas(tarjetasResumen);

            response.setCoderr("0000");
            response.setMessage("Resumen mensual obtenido exitosamente.");
            response.setData(resumen);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener el resumen mensual");
        }

        return response;
    }

    private List<TransaccionConCategoriaDto> enriquecerConCategoria(String uid, List<TransaccionDto> transacciones)
            throws Exception {

        List<TransaccionConCategoriaDto> resultado = new ArrayList<>();

        for (TransaccionDto t : transacciones) {
            TransaccionConCategoriaDto dto = new TransaccionConCategoriaDto();
            dto.setId(t.getId());
            dto.setFecha(t.getFecha());
            dto.setImporte(t.getImporte());
            dto.setCatIngresoId(t.getCatIngresoId());
            dto.setCatEgresoId(t.getCatEgresoId());
            dto.setCuentaId(t.getCuentaId());
            dto.setTarjetaId(t.getTarjetaId());
            dto.setConcepto(t.getConcepto());
            dto.setDescripcion(t.getDescripcion());
            dto.setTipo(t.getTipo());
            dto.setNecesario(t.getNecesario());
            dto.setTransferencia(t.getTransferencia());
            dto.setMsiId(t.getMsiId());

            CategoriaDto categoria = null;
            if (t.getCatIngresoId() != null && !t.getCatIngresoId().isEmpty()) {
                categoria = categoriasRepository.getCategoriaById(uid, t.getCatIngresoId());
            } else if (t.getCatEgresoId() != null && !t.getCatEgresoId().isEmpty()) {
                categoria = categoriasRepository.getCategoriaById(uid, t.getCatEgresoId());
            }
            dto.setCategoria(categoria);

            resultado.add(dto);
        }

        return resultado;
    }
}

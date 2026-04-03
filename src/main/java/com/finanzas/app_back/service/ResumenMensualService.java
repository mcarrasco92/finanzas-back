package com.finanzas.app_back.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.finanzas.app_back.model.Transferencia;
import com.finanzas.app_back.repositories.CategoriasRepository;
import com.finanzas.app_back.repositories.CuentasRepository;
import com.finanzas.app_back.repositories.SpaceRepository;
import com.finanzas.app_back.repositories.TarjetasRepository;
import com.finanzas.app_back.repositories.TransaccionesRepository;
import com.finanzas.app_back.repositories.TransferenciasRepository;

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
    private TransferenciasRepository transferenciasRepository;

    @Autowired
    private CategoriasRepository categoriasRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    public GenericResponse obtenerResumenMensual(String spaceId, String uid, int mes, int anio) {
        GenericResponse response = new GenericResponse();
        try {
            spaceRepository.validateMembership(spaceId, uid);

            String yearMonth = String.format("%04d-%02d", anio, mes);

            ArrayList<CuentaDto> cuentas = cuentasRepository.getCuentas(spaceId);
            ArrayList<TarjetaDto> tarjetas = tarjetasRepository.getTarjetas(spaceId);
            Map<String, CategoriaDto> categoriasMap = buildCategoriasMap(spaceId);
            ArrayList<TransaccionDto> todasTransacciones = transaccionesRepository.getTransaccionesByMonth(spaceId, yearMonth);
            ArrayList<Transferencia> todasTransferencias = transferenciasRepository.getTransferenciasByMonth(spaceId, yearMonth);

            Map<String, List<TransaccionDto>> txPorCuenta = new HashMap<>();
            Map<String, List<TransaccionDto>> txPorTarjeta = new HashMap<>();
            for (TransaccionDto t : todasTransacciones) {
                if (t.getCuentaId() != null && !t.getCuentaId().isEmpty()) {
                    txPorCuenta.computeIfAbsent(t.getCuentaId(), k -> new ArrayList<>()).add(t);
                } else if (t.getTarjetaId() != null && !t.getTarjetaId().isEmpty()) {
                    txPorTarjeta.computeIfAbsent(t.getTarjetaId(), k -> new ArrayList<>()).add(t);
                }
            }

            Map<String, List<TransaccionDto>> transferenciasPorCuenta = buildTransferenciasPorCuenta(todasTransferencias);

            List<CuentaResumenDto> cuentasResumen = new ArrayList<>();
            for (CuentaDto cuenta : cuentas) {
                List<TransaccionDto> todas = new ArrayList<>();
                todas.addAll(txPorCuenta.getOrDefault(cuenta.getId(), List.of()));
                todas.addAll(transferenciasPorCuenta.getOrDefault(cuenta.getId(), List.of()));

                CuentaResumenDto cr = buildCuentaResumen(cuenta, todas, categoriasMap);
                cuentasResumen.add(cr);
            }

            List<TarjetaResumenDto> tarjetasResumen = new ArrayList<>();
            for (TarjetaDto tarjeta : tarjetas) {
                List<TransaccionDto> txTarjeta = txPorTarjeta.getOrDefault(tarjeta.getId(), List.of());
                TarjetaResumenDto tr = buildTarjetaResumen(tarjeta, txTarjeta, categoriasMap);
                tarjetasResumen.add(tr);
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

    public GenericResponse obtenerResumenAnual(String spaceId, String uid, int anio) {
        GenericResponse response = new GenericResponse();
        try {
            spaceRepository.validateMembership(spaceId, uid);

            String year = String.format("%04d", anio);

            // 6 queries total en lugar de 100+
            ArrayList<CuentaDto> cuentas = cuentasRepository.getCuentas(spaceId);
            ArrayList<TarjetaDto> tarjetas = tarjetasRepository.getTarjetas(spaceId);
            Map<String, CategoriaDto> categoriasMap = buildCategoriasMap(spaceId);
            ArrayList<TransaccionDto> todasTransacciones = transaccionesRepository.getTransaccionesByYear(spaceId, year);
            ArrayList<Transferencia> todasTransferencias = transferenciasRepository.getTransferenciasByYear(spaceId, year);

            // Agrupar transacciones por cuentaId -> yearMonth
            Map<String, Map<String, List<TransaccionDto>>> txPorCuenta = new HashMap<>();
            // Agrupar transacciones por tarjetaId -> yearMonth
            Map<String, Map<String, List<TransaccionDto>>> txPorTarjeta = new HashMap<>();
            for (TransaccionDto t : todasTransacciones) {
                if (t.getFecha() == null || t.getFecha().length() < 7) continue;
                String ym = t.getFecha().substring(0, 7);
                if (t.getCuentaId() != null && !t.getCuentaId().isEmpty()) {
                    txPorCuenta.computeIfAbsent(t.getCuentaId(), k -> new HashMap<>())
                               .computeIfAbsent(ym, k -> new ArrayList<>()).add(t);
                } else if (t.getTarjetaId() != null && !t.getTarjetaId().isEmpty()) {
                    txPorTarjeta.computeIfAbsent(t.getTarjetaId(), k -> new HashMap<>())
                                .computeIfAbsent(ym, k -> new ArrayList<>()).add(t);
                }
            }

            // Agrupar transferencias por cuentaId -> yearMonth
            Map<String, Map<String, List<TransaccionDto>>> transfPorCuenta = new HashMap<>();
            for (Transferencia tf : todasTransferencias) {
                if (tf.getFecha() == null || tf.getFecha().length() < 7) continue;
                String ym = tf.getFecha().substring(0, 7);

                TransaccionDto egreso = transferenciaToEgreso(tf);
                transfPorCuenta.computeIfAbsent(tf.getCuentaOrigenId(), k -> new HashMap<>())
                               .computeIfAbsent(ym, k -> new ArrayList<>()).add(egreso);

                if ("Cuenta".equals(tf.getTipoCuentaDestino())) {
                    TransaccionDto ingreso = transferenciaToIngreso(tf);
                    transfPorCuenta.computeIfAbsent(tf.getCuentaDestinoId(), k -> new HashMap<>())
                                   .computeIfAbsent(ym, k -> new ArrayList<>()).add(ingreso);
                }
            }

            List<ResumenMensualDto> resumenAnual = new ArrayList<>();
            for (int mes = 1; mes <= 12; mes++) {
                String yearMonth = String.format("%04d-%02d", anio, mes);

                List<CuentaResumenDto> cuentasResumen = new ArrayList<>();
                for (CuentaDto cuenta : cuentas) {
                    List<TransaccionDto> todas = new ArrayList<>();
                    todas.addAll(txPorCuenta.getOrDefault(cuenta.getId(), Map.of()).getOrDefault(yearMonth, List.of()));
                    todas.addAll(transfPorCuenta.getOrDefault(cuenta.getId(), Map.of()).getOrDefault(yearMonth, List.of()));
                    cuentasResumen.add(buildCuentaResumen(cuenta, todas, categoriasMap));
                }

                List<TarjetaResumenDto> tarjetasResumen = new ArrayList<>();
                for (TarjetaDto tarjeta : tarjetas) {
                    List<TransaccionDto> txTarjeta = txPorTarjeta.getOrDefault(tarjeta.getId(), Map.of()).getOrDefault(yearMonth, List.of());
                    tarjetasResumen.add(buildTarjetaResumen(tarjeta, txTarjeta, categoriasMap));
                }

                ResumenMensualDto resumenMes = new ResumenMensualDto();
                resumenMes.setMes(mes);
                resumenMes.setAnio(anio);
                resumenMes.setCuentas(cuentasResumen);
                resumenMes.setTarjetas(tarjetasResumen);
                resumenAnual.add(resumenMes);
            }

            response.setCoderr("0000");
            response.setMessage("Resumen anual obtenido exitosamente.");
            response.setData(resumenAnual);

        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener el resumen anual");
        }
        return response;
    }

    // --- helpers ---

    private Map<String, CategoriaDto> buildCategoriasMap(String spaceId) throws Exception {
        ArrayList<CategoriaDto> lista = categoriasRepository.getCategorias(spaceId);
        Map<String, CategoriaDto> map = new HashMap<>();
        for (CategoriaDto cat : lista) {
            map.put(cat.getId(), cat);
        }
        return map;
    }

    private Map<String, List<TransaccionDto>> buildTransferenciasPorCuenta(ArrayList<Transferencia> transferencias) {
        Map<String, List<TransaccionDto>> map = new HashMap<>();
        for (Transferencia tf : transferencias) {
            map.computeIfAbsent(tf.getCuentaOrigenId(), k -> new ArrayList<>()).add(transferenciaToEgreso(tf));
            if ("Cuenta".equals(tf.getTipoCuentaDestino())) {
                map.computeIfAbsent(tf.getCuentaDestinoId(), k -> new ArrayList<>()).add(transferenciaToIngreso(tf));
            }
        }
        return map;
    }

    private TransaccionDto transferenciaToEgreso(Transferencia tf) {
        TransaccionDto t = new TransaccionDto();
        t.setFecha(tf.getFecha());
        t.setImporte(tf.getImporte());
        t.setCuentaId(tf.getCuentaOrigenId());
        t.setTipo("Egreso");
        t.setConcepto(tf.getConcepto());
        t.setTransferencia(true);
        String nombreDestino = tf.getNombreCuentaDestino() != null ? tf.getNombreCuentaDestino() : "";
        if ("Cuenta".equals(tf.getTipoCuentaDestino())) {
            t.setDescripcion("Transferencia a " + nombreDestino);
        } else if ("Tarjeta".equals(tf.getTipoCuentaDestino())) {
            t.setDescripcion("Pago a " + nombreDestino);
        } else {
            t.setDescripcion("Transferencia a cuenta");
        }
        return t;
    }

    private TransaccionDto transferenciaToIngreso(Transferencia tf) {
        TransaccionDto t = new TransaccionDto();
        t.setFecha(tf.getFecha());
        t.setImporte(tf.getImporte());
        t.setCuentaId(tf.getCuentaDestinoId());
        t.setTipo("Ingreso");
        t.setConcepto(tf.getConcepto());
        t.setTransferencia(true);
        String nombreOrigen = tf.getNombreCuentaOrigen() != null ? tf.getNombreCuentaOrigen() : "";
        t.setDescripcion("Transferencia de " + nombreOrigen);
        return t;
    }

    private CuentaResumenDto buildCuentaResumen(CuentaDto cuenta, List<TransaccionDto> transacciones, Map<String, CategoriaDto> categoriasMap) {
        CuentaResumenDto cr = new CuentaResumenDto();
        cr.setId(cuenta.getId());
        cr.setNombre(cuenta.getNombre());
        cr.setDescripcion(cuenta.getDescripcion());
        cr.setInstitucion(cuenta.getInstitucion());
        cr.setSaldo(cuenta.getSaldo());
        cr.setInversion(cuenta.isInversion());
        cr.setVista(cuenta.isVista());
        cr.setActiva(cuenta.isActiva());
        cr.setOrden(cuenta.getOrden());
        cr.setTransacciones(enriquecerConCategoriaMap(transacciones, categoriasMap));
        return cr;
    }

    private TarjetaResumenDto buildTarjetaResumen(TarjetaDto tarjeta, List<TransaccionDto> transacciones, Map<String, CategoriaDto> categoriasMap) {
        TarjetaResumenDto tr = new TarjetaResumenDto();
        tr.setId(tarjeta.getId());
        tr.setNombre(tarjeta.getNombre());
        tr.setDescripcion(tarjeta.getDescripcion());
        tr.setInstitucion(tarjeta.getInstitucion());
        tr.setSaldo(tarjeta.getSaldo());
        tr.setDpago(tarjeta.getDpago());
        tr.setDcorte(tarjeta.getDcorte());
        tr.setActiva(tarjeta.isActiva());
        tr.setOrden(tarjeta.getOrden());
        tr.setTransacciones(enriquecerConCategoriaMap(transacciones, categoriasMap));
        return tr;
    }

    private List<TransaccionConCategoriaDto> enriquecerConCategoriaMap(List<TransaccionDto> transacciones, Map<String, CategoriaDto> categoriasMap) {
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
            dto.setTransferencia(t.getTransferencia());
            dto.setMsiId(t.getMsiId());

            CategoriaDto categoria = null;
            if (t.getCatIngresoId() != null && !t.getCatIngresoId().isEmpty()) {
                categoria = categoriasMap.get(t.getCatIngresoId());
            } else if (t.getCatEgresoId() != null && !t.getCatEgresoId().isEmpty()) {
                categoria = categoriasMap.get(t.getCatEgresoId());
            }
            dto.setCategoria(categoria);
            resultado.add(dto);
        }
        return resultado;
    }
}

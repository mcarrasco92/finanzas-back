package com.finanzas.app_back.repositories;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.finanzas.app_back.model.TransaccionRecurrente;
import com.finanzas.app_back.service.TransaccionesService;
import com.finanzas.app_back.dto.TransaccionRecurrente.TransaccionRecurrenteDto;
import com.finanzas.app_back.dto.Transacciones.TransaccionDto;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.firestore.Firestore;

@Repository
public class TransaccionRecurrenteRepository {
    @Autowired
    private Firestore firestore;
    private static final String COLLECTION_NAME = "transaccionesRecurrentes";

    @Autowired
    private com.finanzas.app_back.service.TransaccionesService transaccionesService;

    public String newTransaccionRecurrente(String spaceId, TransaccionRecurrente transaccion) throws Exception {

        CollectionReference colRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME);
        DocumentReference document = colRef.document();

        LocalDate hoy = LocalDate.now();
        Boolean ejecutarHoy = false;

        if (transaccion.getPeriodicidad().equals("Semanal")) {
            DayOfWeek objetivo = switch (transaccion.getDia().toLowerCase()) {
                case "lunes" -> DayOfWeek.MONDAY;
                case "martes" -> DayOfWeek.TUESDAY;
                case "miercoles" -> DayOfWeek.WEDNESDAY;
                case "jueves" -> DayOfWeek.THURSDAY;
                case "viernes" -> DayOfWeek.FRIDAY;
                case "sabado" -> DayOfWeek.SATURDAY;
                case "domingo" -> DayOfWeek.SUNDAY;
                default -> null;
            };
            int diasHastaObjetivo = objetivo.getValue() - hoy.getDayOfWeek().getValue();
            if (diasHastaObjetivo == 0) {
                ejecutarHoy = true;
            }
        } else {
            if (hoy.equals(LocalDate.parse(transaccion.getFecha()))) {
                ejecutarHoy = true;
            }
        }

        if (ejecutarHoy) {
            System.out.println("Es hoy la fecha de ejecución para la transacción recurrente: " + transaccion);
            TransaccionDto movimiento = new TransaccionDto();
            movimiento.setFecha(hoy.toString());
            movimiento.setImporte(transaccion.getImporte());
            movimiento.setCatEgresoId(transaccion.getCatEgresoId());
            movimiento.setCatIngresoId(transaccion.getCatIngresoId());
            movimiento.setTarjetaId(transaccion.getTarjetaId());
            movimiento.setCuentaId(transaccion.getCuentaId());
            movimiento.setConcepto(transaccion.getConcepto());
            movimiento.setDescripcion(transaccion.getDescripcion());
            movimiento.setTipo(transaccion.getTipo());
            transaccionesService.registrarTransaccion(spaceId, movimiento);
        }

        transaccion.setSiguienteEjecucion(calcularSiguienteEjecucion(transaccion));

        ApiFuture<WriteResult> future = document.set(transaccion);
        future.get();
        return document.getId();
    }

    public ArrayList<TransaccionRecurrenteDto> getTransaccionesRecurrentes(String spaceId) throws Exception {
        ArrayList<TransaccionRecurrenteDto> list = new ArrayList<>();
        CollectionReference colRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = colRef.get();
        java.util.List<QueryDocumentSnapshot> documents = query.get().getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            TransaccionRecurrenteDto dto = document.toObject(TransaccionRecurrenteDto.class);
            dto.setId(document.getId());
            list.add(dto);
        }
        return list;
    }

    public TransaccionRecurrenteDto getTransaccionRecurrenteById(String spaceId, String id) throws Exception {
        DocumentReference docRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(id);
        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) return null;
        TransaccionRecurrenteDto dto = snapshot.toObject(TransaccionRecurrenteDto.class);
        dto.setId(snapshot.getId());
        return dto;
    }

    public void deleteTransaccionRecurrente(String spaceId, String id) throws Exception {
        DocumentReference docRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(id);
        docRef.delete().get();
    }

    public String actualizarTransaccionRecurrente(String spaceId, String id, TransaccionRecurrente transaccion) throws Exception {
        DocumentReference docRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(id);

        LocalDate hoy = LocalDate.now();
        Boolean ejecutarHoy = false;
        System.out.println("Evaluando actualización de transacción recurrente: " + transaccion);

        if (transaccion.getPeriodicidad().equals("Semanal")) {
            DayOfWeek objetivo = switch (transaccion.getDia().toLowerCase()) {
                case "lunes" -> DayOfWeek.MONDAY;
                case "martes" -> DayOfWeek.TUESDAY;
                case "miercoles" -> DayOfWeek.WEDNESDAY;
                case "jueves" -> DayOfWeek.THURSDAY;
                case "viernes" -> DayOfWeek.FRIDAY;
                case "sabado" -> DayOfWeek.SATURDAY;
                case "domingo" -> DayOfWeek.SUNDAY;
                default -> null;
            };
            int diasHastaObjetivo = objetivo.getValue() - hoy.getDayOfWeek().getValue();

            System.out.println("diasHastaObjetivo: " + diasHastaObjetivo);
            System.out.println("hoy.getDayOfWeek().getValue(): " + hoy.getDayOfWeek().getValue());
            System.out.println("objetivo.getValue(): " + objetivo.getValue());
            if (diasHastaObjetivo == 0) {
                ejecutarHoy = true;
            }
        } else {
            if (hoy.equals(LocalDate.parse(transaccion.getFecha()))) {
                ejecutarHoy = true;
            }
        }

        if (ejecutarHoy) {
            System.out.println("Es hoy la fecha de ejecución para la transacción recurrente: " + transaccion);
            TransaccionDto movimiento = new TransaccionDto();
            movimiento.setFecha(hoy.toString());
            movimiento.setImporte(transaccion.getImporte());
            movimiento.setCatEgresoId(transaccion.getCatEgresoId());
            movimiento.setCatIngresoId(transaccion.getCatIngresoId());
            movimiento.setTarjetaId(transaccion.getTarjetaId());
            movimiento.setCuentaId(transaccion.getCuentaId());
            movimiento.setConcepto(transaccion.getConcepto());
            movimiento.setDescripcion(transaccion.getDescripcion());
            movimiento.setTipo(transaccion.getTipo());
            transaccionesService.registrarTransaccion(spaceId, movimiento);
        }

        transaccion.setSiguienteEjecucion(calcularSiguienteEjecucion(transaccion));

        docRef.set(transaccion).get();
        return id;
    }

    public void generaTransaccionesRecurrentes(TransaccionesService transaccionesService) throws Exception {
        System.out.println("Iniciando generación de transacciones recurrentes...");
        CollectionReference spacesRef = firestore.collection("spaces");
        ApiFuture<QuerySnapshot> spacesQuery = spacesRef.get();
        java.util.List<QueryDocumentSnapshot> spaceDocuments = spacesQuery.get().getDocuments();

        System.out.println("Spaces encontrados: " + spaceDocuments.size());

        for (QueryDocumentSnapshot spaceDoc : spaceDocuments) {
            String spaceId = spaceDoc.getId();
            CollectionReference colRef = spacesRef.document(spaceId).collection(COLLECTION_NAME);
            ApiFuture<QuerySnapshot> transRecQuery = colRef.get();
            System.out.println("Procesando transacciones recurrentes para el space: " + spaceId);
            for (QueryDocumentSnapshot transRecDoc : transRecQuery.get().getDocuments()) {
                TransaccionRecurrente dto = transRecDoc.toObject(TransaccionRecurrente.class);
                LocalDate hoy = LocalDate.now();

                System.out.println("Evaluando transacción recurrente: " + dto);
                System.out.println("Fecha de siguiente ejecución: " + dto.getSiguienteEjecucion());

                if (dto.getSiguienteEjecucion() != null && !dto.getSiguienteEjecucion().isEmpty()) {
                    LocalDate siguienteEjecucion = LocalDate.parse(dto.getSiguienteEjecucion());
                    if (siguienteEjecucion.isEqual(hoy)) {

                        System.out.println("Es hoy la fecha de ejecución para la transacción recurrente: " + dto);
                        TransaccionDto movimiento = new TransaccionDto();
                        movimiento.setFecha(hoy.toString());
                        movimiento.setImporte(dto.getImporte());
                        movimiento.setCatEgresoId(dto.getCatEgresoId());
                        movimiento.setCatIngresoId(dto.getCatIngresoId());
                        movimiento.setTarjetaId(dto.getTarjetaId());
                        movimiento.setCuentaId(dto.getCuentaId());
                        movimiento.setConcepto(dto.getConcepto());
                        movimiento.setDescripcion(dto.getDescripcion());
                        movimiento.setTipo(dto.getTipo());
                        transaccionesService.registrarTransaccion(spaceId, movimiento);
                        String nuevaEjecucion = calcularSiguienteEjecucion(dto);
                        DocumentReference transRecRef = colRef.document(transRecDoc.getId());
                        transRecRef.update("siguienteEjecucion", nuevaEjecucion).get();
                    }
                }
            }
        }
    }

    private String calcularSiguienteDiaSemana(String diaSemana) {
        LocalDate hoy = LocalDate.now();
        DayOfWeek objetivo = switch (diaSemana.toLowerCase()) {
            case "lunes" -> DayOfWeek.MONDAY;
            case "martes" -> DayOfWeek.TUESDAY;
            case "miercoles" -> DayOfWeek.WEDNESDAY;
            case "jueves" -> DayOfWeek.THURSDAY;
            case "viernes" -> DayOfWeek.FRIDAY;
            case "sabado" -> DayOfWeek.SATURDAY;
            case "domingo" -> DayOfWeek.SUNDAY;
            default -> null;
        };
        if (objetivo == null) return "";
        int diasHastaObjetivo = objetivo.getValue() - hoy.getDayOfWeek().getValue();
        if (diasHastaObjetivo <= 0) diasHastaObjetivo += 7;
        LocalDate siguiente = hoy.plusDays(diasHastaObjetivo);
        System.out.println("siguiente dia de la semana " + diaSemana + ": " + siguiente);
        return siguiente.toString();
    }

    public String calcularSiguienteFecha(LocalDate fecha, String periodicidad) {
        LocalDate hoy = LocalDate.now();
        LocalDate siguiente;
        System.out.println("Calculando siguiente fecha para periodicidad: " + periodicidad);
        System.out.println("Fecha actual: " + fecha);
        switch (periodicidad.toLowerCase()) {
            case "mensual":
                siguiente = fecha.plusMonths(1);
                break;
            case "bimestral":
                siguiente = fecha.plusMonths(2);
                break;
            case "semestral":
                siguiente = fecha.plusMonths(6);
                break;
            case "anual":
                siguiente = fecha.plusYears(1);
                break;
            default:
                siguiente = fecha;
        }
        if (fecha.isAfter(hoy)) {
            return fecha.toString();
        }
        System.out.println("siguiente fecha para periodicidad " + periodicidad + ": " + siguiente);
        return siguiente.toString();
    }

    private String calcularSiguienteEjecucion(TransaccionRecurrente transaccion) {
        String siguienteEjecucion = "";

        System.out.println("Calculando siguiente ejecución para periodicidad: " + transaccion.getPeriodicidad());

        if (transaccion.getPeriodicidad().equals("Semanal")) {
            siguienteEjecucion = calcularSiguienteDiaSemana(transaccion.getDia());
        } else {
            siguienteEjecucion = calcularSiguienteFecha(LocalDate.parse(transaccion.getFecha()), transaccion.getPeriodicidad());
        }

        return siguienteEjecucion;
    }
}

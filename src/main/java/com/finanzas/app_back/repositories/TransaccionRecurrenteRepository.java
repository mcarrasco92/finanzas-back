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

    public String newTransaccionRecurrente(String uid, TransaccionRecurrente transaccion) throws Exception {

        CollectionReference colRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME);
        DocumentReference document = colRef.document();

        LocalDate hoy = LocalDate.now();
        Boolean ejecutarHoy = false;

        if(transaccion.getPeriodicidad().equals("Semanal")) {
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
            if (diasHastaObjetivo == 0){
                ejecutarHoy = true;
            }

        }else{
            if(hoy.equals(LocalDate.parse(transaccion.getFecha()))){
                ejecutarHoy = true;
            }
        }

        if(ejecutarHoy){
            System.out.println("Es hoy la fecha de ejecución para la transacción recurrente: " + transaccion);
                // Crear el movimiento usando TransaccionesService
                TransaccionDto movimiento = new TransaccionDto();
                movimiento.setFecha(hoy.toString());
                movimiento.setImporte(transaccion.getImporte());
                movimiento.setCatEgresoId(transaccion.getCatEgresoId());
                movimiento.setCatIngresoId(transaccion.getCatIngresoId());
                movimiento.setTarjetaId(transaccion.getTarjetaId());
                movimiento.setCuentaId(transaccion.getCuentaId());
                movimiento.setConcepto(transaccion.getConcepto());
                movimiento.setDescripcion(transaccion.getDescripcion());
                movimiento.setNecesario(transaccion.getNecesario());
                movimiento.setTipo(transaccion.getTipo());
                transaccionesService.registrarTransaccion(uid, movimiento);
        }

        transaccion.setSiguienteEjecucion(calcularSiguienteEjecucion(transaccion));

        
        ApiFuture<WriteResult> future = document.set(transaccion);
        future.get();
        return document.getId();
    }

    public ArrayList<TransaccionRecurrenteDto> getTransaccionesRecurrentes(String uid) throws Exception {
        ArrayList<TransaccionRecurrenteDto> list = new ArrayList<>();
        CollectionReference colRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = colRef.get();
        java.util.List<QueryDocumentSnapshot> documents = query.get().getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            TransaccionRecurrenteDto dto = document.toObject(TransaccionRecurrenteDto.class);
            dto.setId(document.getId());
            list.add(dto);
        }
        return list;
    }

    public TransaccionRecurrenteDto getTransaccionRecurrenteById(String uid, String id) throws Exception {
        DocumentReference docRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(id);
        DocumentSnapshot snapshot = docRef.get().get();
        if (!snapshot.exists()) return null;
        TransaccionRecurrenteDto dto = snapshot.toObject(TransaccionRecurrenteDto.class);
        dto.setId(snapshot.getId());
        return dto;
    }

    public void deleteTransaccionRecurrente(String uid, String id) throws Exception {
        DocumentReference docRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(id);
        docRef.delete().get();
    }

    public String actualizarTransaccionRecurrente(String uid, String id, TransaccionRecurrente transaccion) throws Exception {
        DocumentReference docRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(id);

        LocalDate hoy = LocalDate.now();
        Boolean ejecutarHoy = false;
        System.out.println("Evaluando actualización de transacción recurrente: " + transaccion);

        if(transaccion.getPeriodicidad().equals("Semanal")) {
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
            if (diasHastaObjetivo == 0){
                ejecutarHoy = true;
            }

        }else{
            if(hoy.equals(LocalDate.parse(transaccion.getFecha()))){
                ejecutarHoy = true;
            }
        }

        if(ejecutarHoy){
            System.out.println("Es hoy la fecha de ejecución para la transacción recurrente: " + transaccion);
                // Crear el movimiento usando TransaccionesService
                TransaccionDto movimiento = new TransaccionDto();
                movimiento.setFecha(hoy.toString());
                movimiento.setImporte(transaccion.getImporte());
                movimiento.setCatEgresoId(transaccion.getCatEgresoId());
                movimiento.setCatIngresoId(transaccion.getCatIngresoId());
                movimiento.setTarjetaId(transaccion.getTarjetaId());
                movimiento.setCuentaId(transaccion.getCuentaId());
                movimiento.setConcepto(transaccion.getConcepto());
                movimiento.setDescripcion(transaccion.getDescripcion());
                movimiento.setNecesario(transaccion.getNecesario());
                movimiento.setTipo(transaccion.getTipo());
                transaccionesService.registrarTransaccion(uid, movimiento);
        }

        transaccion.setSiguienteEjecucion(calcularSiguienteEjecucion(transaccion));

        docRef.set(transaccion).get();
        return id;
    }

    public void generaTransaccionesRecurrentes(TransaccionesService transaccionesService) throws Exception {
        // Obtener todos los usuarios
        System.out.println("Iniciando generación de transacciones recurrentes...");
        CollectionReference usersRef = firestore.collection("users");
        ApiFuture<QuerySnapshot> usersQuery = usersRef.get();
        java.util.List<QueryDocumentSnapshot> documents = usersQuery.get().getDocuments();


        System.out.println("Usuarios encontrados: " + documents.size());


        for (QueryDocumentSnapshot userDoc : usersQuery.get().getDocuments()) {
            String uid = userDoc.getId();
            // Obtener todas las transacciones recurrentes del usuario
            CollectionReference colRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME);
            ApiFuture<QuerySnapshot> transRecQuery = colRef.get();
            System.out.println("Procesando transacciones recurrentes para el usuario: " + uid);
            for (QueryDocumentSnapshot transRecDoc : transRecQuery.get().getDocuments()) {
                TransaccionRecurrente dto = transRecDoc.toObject(TransaccionRecurrente.class);
                // Verificar si la siguiente ejecución es hoy
                LocalDate hoy = LocalDate.now();

                System.out.println("Evaluando transacción recurrente: " + dto);
                System.out.println("Fecha de siguiente ejecución: " + dto.getSiguienteEjecucion());

                if (dto.getSiguienteEjecucion() != null && !dto.getSiguienteEjecucion().isEmpty()) {
                    LocalDate siguienteEjecucion = LocalDate.parse(dto.getSiguienteEjecucion());
                    if (siguienteEjecucion.isEqual(hoy)) {

                        System.out.println("Es hoy la fecha de ejecución para la transacción recurrente: " + dto);
                        // Crear el movimiento usando TransaccionesService
                        TransaccionDto movimiento = new TransaccionDto();
                        movimiento.setFecha(hoy.toString());
                        movimiento.setImporte(dto.getImporte());
                        movimiento.setCatEgresoId(dto.getCatEgresoId());
                        movimiento.setCatIngresoId(dto.getCatIngresoId());
                        movimiento.setTarjetaId(dto.getTarjetaId());
                        movimiento.setCuentaId(dto.getCuentaId());
                        movimiento.setConcepto(dto.getConcepto());
                        movimiento.setDescripcion(dto.getDescripcion());
                        movimiento.setNecesario(dto.getNecesario());
                        movimiento.setTipo(dto.getTipo());
                        transaccionesService.registrarTransaccion(uid, movimiento);
                        // Calcular el nuevo día de ejecución
                        String nuevaEjecucion = calcularSiguienteEjecucion(dto);
                        // Actualizar el campo siguienteEjecucion
                        DocumentReference transRecRef = colRef.document(transRecDoc.getId());
                        transRecRef.update("siguienteEjecucion", nuevaEjecucion.toString()).get();
                    }
                }
            }
        }
    }

    private String calcularSiguienteDiaSemana(String diaSemana) {
        // Obtener la fecha actual
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
        return siguiente.toString(); // Formato yyyy-MM-dd
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
        // Si la fecha ingresada es mayor al día de hoy, conservar esa fecha
        if (fecha.isAfter(hoy)) {
            return fecha.toString();
        }
        System.out.println("siguiente fecha para periodicidad " + periodicidad + ": " + siguiente);
        return siguiente.toString(); // Formato yyyy-MM-dd
    }

    private String calcularSiguienteEjecucion(TransaccionRecurrente transaccion) {
        // Lógica para calcular la siguiente ejecución basada en la periodicidad

        String siguienteEjecucion = "";

        System.out.println("Calculando siguiente ejecución para periodicidad: " + transaccion.getPeriodicidad());

        if (transaccion.getPeriodicidad().equals("Semanal")) {
            siguienteEjecucion = calcularSiguienteDiaSemana(transaccion.getDia());
        }else{
            siguienteEjecucion = calcularSiguienteFecha(LocalDate.parse(transaccion.getFecha()), transaccion.getPeriodicidad());
        }

        return siguienteEjecucion;
    }
}

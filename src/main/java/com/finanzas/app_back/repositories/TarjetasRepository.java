package com.finanzas.app_back.repositories;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.finanzas.app_back.dto.Tarjetas.TarjetaDto;
import com.finanzas.app_back.model.Tarjeta;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@Repository
public class TarjetasRepository {
        @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "tarjetas";

    public String newTarjeta(String uid, Tarjeta tarjeta) throws ExecutionException, InterruptedException {

        System.out.println("Tarjeta a registrar: " + tarjeta);
        
        CollectionReference tarjetas = firestore.collection("users").document(uid).collection(COLLECTION_NAME);
        DocumentReference document = tarjetas.document();

        ApiFuture<WriteResult> writeResult = document.set(tarjeta);
        writeResult.get();

        return document.getId(); // Retorna el ID del documento creado
    }

    public ArrayList<TarjetaDto> getTarjetas(String uid) throws ExecutionException, InterruptedException {
        CollectionReference tarjetasRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> querySnapshot = tarjetasRef.get();

        ArrayList<TarjetaDto> tarjetasList = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
            TarjetaDto tarjeta = document.toObject(TarjetaDto.class);
            tarjeta.setId(document.getId()); // Asigna el ID del documento a la tarjeta

            tarjetasList.add(tarjeta);
        }

        return tarjetasList;
    }

    public TarjetaDto getTarjetaById(String uid, String tarjetaId) throws ExecutionException, InterruptedException {
        DocumentReference tarjetaRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(tarjetaId);
        ApiFuture<DocumentSnapshot> future = tarjetaRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            TarjetaDto tarjeta = document.toObject(TarjetaDto.class);
            tarjeta.setId(document.getId()); // Asigna el ID del documento a la tarjeta
            return tarjeta;
        } else {
            return null; // O lanza una excepción si prefieres
        }
    }

    public void updateTarjeta(String uid, String tarjetaId, Tarjeta tarjeta) throws ExecutionException, InterruptedException {
        DocumentReference tarjetaRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(tarjetaId);
        ApiFuture<WriteResult> writeResult = tarjetaRef.set(tarjeta);
        writeResult.get(); // Espera a que la operación se complete
    }

    public void deleteTarjeta(String uid, String tarjetaId) throws ExecutionException, InterruptedException {
        DocumentReference tarjetaRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(tarjetaId);
        ApiFuture<WriteResult> writeResult = tarjetaRef.delete();
        writeResult.get(); // Espera a que la operación se complete
    }


    public double pagoPendiente(String uid, String tarjetaId) throws ExecutionException, InterruptedException {
        DocumentReference tarjetaRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(tarjetaId);
        ApiFuture<DocumentSnapshot> future = tarjetaRef.get();
        DocumentSnapshot document = future.get();


        if (document.exists()) {
            Tarjeta tarjeta = document.toObject(Tarjeta.class);

            double saldoPeriodoActual = saldoPeriodoActual(uid, tarjetaId);

            double SaldoTotal = tarjeta.getSaldo();;

            double pagoPendiente = SaldoTotal - saldoPeriodoActual;

            return pagoPendiente;
            
        } else {
            return 0; // O lanza una excepción si prefieres
        }
    }



    public double saldoPeriodoActual(String uid, String tarjetaId) throws ExecutionException, InterruptedException {
        DocumentReference tarjetaRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(tarjetaId);
        ApiFuture<DocumentSnapshot> future = tarjetaRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Tarjeta tarjeta = document.toObject(Tarjeta.class);

            // Obtener el día de corte como un entero
            int diaCorte = Integer.parseInt(tarjeta.getDcorte());

            // Obtener la fecha actual
            LocalDate fechaActual = LocalDate.now();

            // Calcular la fecha de inicio
            LocalDate fechaInicio = fechaActual.withDayOfMonth(diaCorte);

            fechaInicio = fechaInicio.plusDays(1); // Mover al día siguiente para incluir el día de corte

            if (fechaInicio.getDayOfMonth() < diaCorte) {
                // Si el día actual es menor que el día de corte, retroceder un mes
                fechaInicio = fechaInicio.minusMonths(1);
            }

            // Calcular la fecha de fin (un mes después de la fecha de inicio)
            LocalDate fechaFin = fechaInicio.plusMonths(1).minusDays(1); // Restar un día para incluir el último día del período

            // Convertir las fechas al formato "YYYY-MM-DD"
            String fechaInicioStr = fechaInicio.toString(); // Formato por defecto de LocalDate es "YYYY-MM-DD"
            String fechaFinStr = fechaFin.toString();

            // Consultar las transacciones en el rango de fechas
            CollectionReference transaccionesRef = firestore.collection("users").document(uid).collection("transacciones");
            ApiFuture<QuerySnapshot> snapshot = transaccionesRef
                .whereEqualTo("tarjetaId", tarjetaId)
                .whereGreaterThanOrEqualTo("fecha", fechaInicioStr) // Fecha >= fechaInicio
                .whereLessThan("fecha", fechaFinStr) // Fecha < fechaFin
                .get();

            // Calcular el total pendiente
            double saldoPeriodoActual = 0;
            for (QueryDocumentSnapshot transaccion : snapshot.get().getDocuments()) {
                saldoPeriodoActual += transaccion.getDouble("importe");
            }

            return saldoPeriodoActual;
            
        } else {
            return 0; // O lanza una excepción si prefieres
        }
    }


    

}




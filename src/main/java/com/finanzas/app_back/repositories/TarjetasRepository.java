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

    public String newTarjeta(String spaceId, Tarjeta tarjeta) throws ExecutionException, InterruptedException {

        System.out.println("Tarjeta a registrar: " + tarjeta);

        CollectionReference tarjetas = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME);
        DocumentReference document = tarjetas.document();

        ApiFuture<WriteResult> writeResult = document.set(tarjeta);
        writeResult.get();

        return document.getId();
    }

    public ArrayList<TarjetaDto> getTarjetas(String spaceId) throws ExecutionException, InterruptedException {
        CollectionReference tarjetasRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> querySnapshot = tarjetasRef.get();

        ArrayList<TarjetaDto> tarjetasList = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
            TarjetaDto tarjeta = document.toObject(TarjetaDto.class);
            tarjeta.setId(document.getId());
            tarjetasList.add(tarjeta);
        }

        return tarjetasList;
    }

    public TarjetaDto getTarjetaById(String spaceId, String tarjetaId) throws ExecutionException, InterruptedException {
        DocumentReference tarjetaRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(tarjetaId);
        ApiFuture<DocumentSnapshot> future = tarjetaRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            TarjetaDto tarjeta = document.toObject(TarjetaDto.class);
            tarjeta.setId(document.getId());
            return tarjeta;
        } else {
            return null;
        }
    }

    public void updateTarjeta(String spaceId, String tarjetaId, Tarjeta tarjeta) throws ExecutionException, InterruptedException {
        DocumentReference tarjetaRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(tarjetaId);
        ApiFuture<WriteResult> writeResult = tarjetaRef.set(tarjeta);
        writeResult.get();
    }

    public void deleteTarjeta(String spaceId, String tarjetaId) throws ExecutionException, InterruptedException {
        DocumentReference tarjetaRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(tarjetaId);
        ApiFuture<WriteResult> writeResult = tarjetaRef.delete();
        writeResult.get();
    }


    public double pagoPendiente(String spaceId, String tarjetaId) throws ExecutionException, InterruptedException {
        DocumentReference tarjetaRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(tarjetaId);
        ApiFuture<DocumentSnapshot> future = tarjetaRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Tarjeta tarjeta = document.toObject(Tarjeta.class);

            double saldoPeriodoActual = saldoPeriodoActual(spaceId, tarjetaId);
            double saldoMsiFuturos = saldoMsiFuturos(spaceId, tarjetaId);

            double SaldoTotal = tarjeta.getSaldo();;

            double pagoPendiente = SaldoTotal - saldoPeriodoActual - saldoMsiFuturos;

            return pagoPendiente;

        } else {
            return 0;
        }
    }


    public double saldoPeriodoActual(String spaceId, String tarjetaId) throws ExecutionException, InterruptedException {
        DocumentReference tarjetaRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(tarjetaId);
        ApiFuture<DocumentSnapshot> future = tarjetaRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Tarjeta tarjeta = document.toObject(Tarjeta.class);

            int diaCorte = Integer.parseInt(tarjeta.getDcorte());

            LocalDate fechaActual = LocalDate.now();

            LocalDate fechaInicio = fechaActual.withDayOfMonth(diaCorte);

            fechaInicio = fechaInicio.plusDays(1);

            if (fechaActual.getDayOfMonth() < diaCorte) {
                fechaInicio = fechaInicio.minusMonths(1);
            }

            LocalDate fechaFin = fechaInicio.plusMonths(1).minusDays(1);

            String fechaInicioStr = fechaInicio.toString();
            String fechaFinStr = fechaFin.toString();

            CollectionReference transaccionesRef = firestore.collection("spaces").document(spaceId).collection("transacciones");
            ApiFuture<QuerySnapshot> snapshot = transaccionesRef
                .whereEqualTo("tarjetaId", tarjetaId)
                .whereGreaterThanOrEqualTo("fecha", fechaInicioStr)
                .whereLessThan("fecha", fechaFinStr)
                .get();

            double saldoPeriodoActual = 0;
            for (QueryDocumentSnapshot transaccion : snapshot.get().getDocuments()) {
                saldoPeriodoActual += transaccion.getDouble("importe");
            }

            return saldoPeriodoActual;

        } else {
            return 0;
        }
    }

    public double saldoMsiFuturos(String spaceId, String tarjetaId) throws ExecutionException, InterruptedException {

        DocumentReference tarjetaRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(tarjetaId);
        ApiFuture<DocumentSnapshot> future = tarjetaRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Tarjeta tarjeta = document.toObject(Tarjeta.class);

            int diaCorte = Integer.parseInt(tarjeta.getDcorte());

            LocalDate fechaActual = LocalDate.now();

            LocalDate fechaInicio = fechaActual.withDayOfMonth(diaCorte);

            fechaInicio = fechaInicio.plusDays(1);

            if (fechaActual.getDayOfMonth() < diaCorte) {
                fechaInicio = fechaInicio.minusMonths(1);
            }

            LocalDate fechaFin = fechaInicio.plusMonths(1).minusDays(1);

            String fechaFinStr = fechaFin.toString();

            CollectionReference transaccionesRef = firestore.collection("spaces").document(spaceId).collection("transacciones");
            ApiFuture<QuerySnapshot> snapshot = transaccionesRef
                .whereEqualTo("tarjetaId", tarjetaId)
                .whereGreaterThanOrEqualTo("fecha", fechaFinStr)
                .get();

            double saldoMsiFuturos = 0;
            for (QueryDocumentSnapshot transaccion : snapshot.get().getDocuments()) {
                saldoMsiFuturos += transaccion.getDouble("importe");
            }

            return saldoMsiFuturos;
        } else {
            return 0;
        }
    }


}

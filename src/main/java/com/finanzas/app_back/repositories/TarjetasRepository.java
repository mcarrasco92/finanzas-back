package com.finanzas.app_back.repositories;

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

}

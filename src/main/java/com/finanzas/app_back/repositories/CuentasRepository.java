package com.finanzas.app_back.repositories;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.finanzas.app_back.dto.Cuentas.CuentaDto;
import com.finanzas.app_back.model.Cuenta;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.DocumentSnapshot;

@Repository
public class CuentasRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "cuentas";

    public String newCuenta(String uid, Cuenta cuenta) throws ExecutionException, InterruptedException {

        System.out.println("Cuenta a registrar: " + cuenta);
        
        CollectionReference cuentas = firestore.collection("users").document(uid).collection(COLLECTION_NAME);
        DocumentReference document = cuentas.document();

        ApiFuture<WriteResult> writeResult = document.set(cuenta);
        writeResult.get();

        return document.getId(); // Retorna el ID del documento creado
    }

    public ArrayList<CuentaDto> getCuentas(String uid) throws ExecutionException, InterruptedException {
        CollectionReference cuentasRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> querySnapshot = cuentasRef.get();

        ArrayList<CuentaDto> cuentasList = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
            CuentaDto cuenta = document.toObject(CuentaDto.class);
            cuenta.setId(document.getId()); // Asigna el ID del documento a la cuenta

            cuentasList.add(cuenta);
        }

        return cuentasList;
    }

    public CuentaDto getCuentaById(String uid, String cuentaId) throws ExecutionException, InterruptedException {
        DocumentReference cuentaRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(cuentaId);
        ApiFuture<DocumentSnapshot> future = cuentaRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            CuentaDto cuenta = document.toObject(CuentaDto.class);
            cuenta.setId(document.getId()); // Asigna el ID del documento a la cuenta
            return cuenta;
        } else {
            return null; // O lanza una excepción si prefieres
        }
    }

    public void updateCuenta(String uid, String cuentaId, Cuenta cuenta) throws ExecutionException, InterruptedException {
        DocumentReference cuentaRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(cuentaId);
        ApiFuture<WriteResult> writeResult = cuentaRef.set(cuenta);
        writeResult.get(); // Espera a que la operación se complete
    }

    public void deleteCuenta(String uid, String cuentaId) throws ExecutionException, InterruptedException {
        DocumentReference cuentaRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(cuentaId);
        ApiFuture<WriteResult> writeResult = cuentaRef.delete();
        writeResult.get(); // Espera a que la operación se complete
    }

    
}

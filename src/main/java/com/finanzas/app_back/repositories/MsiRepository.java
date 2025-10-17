package com.finanzas.app_back.repositories;

import java.time.LocalDate;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.finanzas.app_back.dto.Msi.MsiDto;
import com.finanzas.app_back.model.Msi;
import com.finanzas.app_back.model.Transaccion;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firestore.v1.Document;
import com.google.protobuf.Api;

@Repository
public class MsiRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "msi";

    @Autowired
    private TransaccionesRepository transaccionesRepository;

    public String newMsi(String uid, Msi msi) throws Exception {

        System.out.println("MSI a registrar desde repo: " + msi);

        CollectionReference msis = firestore.collection("users").document(uid).collection(COLLECTION_NAME);
        DocumentReference document = msis.document();

        ApiFuture<String> future = firestore.runTransaction(transaction -> {

            LocalDate fechaInicial = LocalDate.parse(msi.getFecha()); // Asegúrate de que msi.getFecha() esté en formato "yyyy-MM-dd"

            for(int i = 0; i < msi.getMeses(); i++) {
                Transaccion trans = new Transaccion();
                trans.setTarjetaId(msi.getTarjetaId());
                trans.setCatEgresoId(msi.getCatEgresoId());
                trans.setConcepto(msi.getConcepto() + " - MSI " + (i + 1) + "/" + msi.getMeses());
                trans.setDescripcion(msi.getDescripcion());
                trans.setImporte(msi.getImporte() / msi.getMeses());
                trans.setNecesario(msi.getNecesario());
                trans.setTipo("Egreso");
                trans.setMsiId(document.getId());

                // Incrementar la fecha un mes por cada iteración
                LocalDate fechaTransaccion = fechaInicial.plusMonths(i);
                trans.setFecha(fechaTransaccion.toString()); // Convertir de nuevo a String en formato "yyyy-MM-dd"

                System.out.println("Transacción a registrar: " + trans);

                

                transaccionesRepository.newTransaccionTarjeta(uid, trans);    
            }

            
            transaction.set(document, msi);
            return document.getId();
        });

        return future.get();
        
    }

    public ArrayList<MsiDto> getMsisByTarjetaId(String uid, String tarjetaId) throws Exception {
        ArrayList<MsiDto> msisList = new ArrayList<>();

        CollectionReference msisRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = msisRef.whereEqualTo("tarjetaId", tarjetaId).get();
        java.util.List<QueryDocumentSnapshot> documents = query.get().getDocuments();

        for (QueryDocumentSnapshot document : documents) {
            MsiDto dto = document.toObject(MsiDto.class);
            dto.setId(document.getId());
            msisList.add(dto);
        }

        return msisList;
    }

    public ArrayList<MsiDto> getMsis(String uid) throws Exception {
        ArrayList<MsiDto> msisList = new ArrayList<>();

        CollectionReference msisRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = msisRef.get();
        java.util.List<QueryDocumentSnapshot> documents = query.get().getDocuments();

        System.out.println("Documentos MSI obtenidos: " + documents.size());

        for (QueryDocumentSnapshot document : documents) {
            MsiDto dto = document.toObject(MsiDto.class);
            dto.setId(document.getId());
            msisList.add(dto);
        }

        System.out.println("MSI List: " + msisList);

        return msisList;
    }


    public void deleteMsi(String uid, String msiId) throws Exception {
        DocumentReference msiRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(msiId);
        CollectionReference transRef = firestore.collection("users").document(uid).collection("transacciones");
        ApiFuture<String> future = firestore.runTransaction(transaction -> {

            ApiFuture<DocumentSnapshot> msiSnapshotFuture = msiRef.get();
            DocumentSnapshot msiSnapshot = msiSnapshotFuture.get();

            Double nuevoSaldoTarjeta = 0.0;

            if (!msiSnapshot.exists()) {
                throw new Exception("El MSI con ID " + msiId + " no existe.");
            }

            DocumentReference tarjetaRef = firestore.collection("users").document(uid).collection("tarjetas").document(msiSnapshot.getString("tarjetaId"));
            // Actualizar el saldo de la tarjeta asociada al MSI
            ApiFuture<DocumentSnapshot> tarjetaSnapshotFuture = tarjetaRef.get();
            DocumentSnapshot tarjetaSnapshot = tarjetaSnapshotFuture.get();

            if (tarjetaSnapshot.exists()) {
                Double saldoActual = tarjetaSnapshot.getDouble("saldo");
                Double importeMsi = msiSnapshot.getDouble("importe");
                if (saldoActual != null && importeMsi != null) {
                    nuevoSaldoTarjeta = saldoActual - importeMsi;
                }else {
                    throw new Exception("No se pudo actualizar el saldo de la tarjeta asociada al MSI - 001.");
                }
            }

            // Eliminar las transacciones asociadas al MSI
            ApiFuture<QuerySnapshot> query = transRef.whereEqualTo("msiId", msiId).get();
            java.util.List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                DocumentReference transDocRef = transRef.document(document.getId());
                transaction.delete(transDocRef);
            }

            // Eliminar el MSI
            transaction.update(tarjetaRef, "saldo", nuevoSaldoTarjeta);
            transaction.delete(msiRef);

            return null;

        });
    }


    public String actualizarMsi(String uid, String msiId, Msi msi) throws Exception {

        DocumentReference msiRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(msiId);
        CollectionReference transRef = firestore.collection("users").document(uid).collection("transacciones");

        ApiFuture<String> future = firestore.runTransaction(transaction -> {

            ApiFuture<DocumentSnapshot> msiSnapshotFuture = msiRef.get();
            DocumentSnapshot msiSnapshot = msiSnapshotFuture.get();
            DocumentReference tarjetaRef = firestore.collection("users").document(uid).collection("tarjetas").document(msiSnapshot.getString("tarjetaId"));

            Double nuevoSaldoTarjeta = 0.0;
            Boolean saldoActualizado = false;

            if (!msiSnapshot.exists()) {
                throw new Exception("El MSI con ID " + msiId + " no existe.");
            }

            Double importeMsiOld = msiSnapshot.getDouble("importe");
            Double importeMsiNew = msi.getImporte();

            if(!importeMsiOld.equals(importeMsiNew)){
                // Actualizar el saldo de la tarjeta asociada al MSI
                ApiFuture<DocumentSnapshot> tarjetaSnapshotFuture = tarjetaRef.get();
                DocumentSnapshot tarjetaSnapshot = tarjetaSnapshotFuture.get();

                if (tarjetaSnapshot.exists()) {
                    Double saldoActual = tarjetaSnapshot.getDouble("saldo");
                    if (saldoActual != null && importeMsiOld != null && importeMsiNew != null) {
                        nuevoSaldoTarjeta = saldoActual - importeMsiOld + importeMsiNew;
                        saldoActualizado = true;
                    }else {
                        throw new Exception("No se pudo actualizar el saldo de la tarjeta asociada al MSI - 002.");
                    }
                }

            }


            // Eliminar las transacciones asociadas al MSI
            ApiFuture<QuerySnapshot> query = transRef.whereEqualTo("msiId", msiId).get();
            java.util.List<QueryDocumentSnapshot> documents = query.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                DocumentReference transDocRef = transRef.document(document.getId());
                transaction.delete(transDocRef);
            }

            // Crear nuevas transacciones asociadas al MSI actualizado
            LocalDate fechaInicial = LocalDate.parse(msi.getFecha()); // Asegúrate de que msi.getFecha() esté en formato "yyyy-MM-dd"
            for(int i = 0; i < msi.getMeses(); i++) {
                Transaccion trans = new Transaccion();
                trans.setTarjetaId(msi.getTarjetaId());
                trans.setCatEgresoId(msi.getCatEgresoId());
                trans.setConcepto(msi.getConcepto() + " - MSI " + (i + 1) + "/" + msi.getMeses());
                trans.setDescripcion(msi.getDescripcion());
                trans.setImporte(msi.getImporte() / msi.getMeses());
                trans.setNecesario(msi.getNecesario());
                trans.setTipo("Egreso");
                trans.setMsiId(msiId);

                // Incrementar la fecha un mes por cada iteración
                LocalDate fechaTransaccion = fechaInicial.plusMonths(i);
                trans.setFecha(fechaTransaccion.toString()); // Convertir de nuevo a String en formato "yyyy-MM-dd"
                transaction.set(transRef.document(), trans);
            }
            
            // Actualizar
            if(saldoActualizado){
                transaction.update(tarjetaRef, "saldo", nuevoSaldoTarjeta);
            }
            
            transaction.set(msiRef, msi);
            return msiId;
        });

        return msiId;
    }

}

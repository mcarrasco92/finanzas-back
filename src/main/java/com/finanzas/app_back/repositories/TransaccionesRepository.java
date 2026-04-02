package com.finanzas.app_back.repositories;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.finanzas.app_back.dto.Transacciones.TransaccionDto;
import com.finanzas.app_back.model.Transaccion;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;

@Repository
public class TransaccionesRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "transacciones";

    public String newTransaccionCuenta(String spaceId, Transaccion transaccion)
            throws ExecutionException, InterruptedException {

        CollectionReference transaccionesRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME);
        DocumentReference cuentaRef = firestore.collection("spaces").document(spaceId).collection("cuentas")
                .document(transaccion.getCuentaId());

        ApiFuture<String> future = firestore.runTransaction(transaction -> {
            DocumentSnapshot cuentaSnapshot = transaction.get(cuentaRef).get();

            if (!cuentaSnapshot.exists()) {
                throw new RuntimeException("La cuenta especificada no existe.");
            }

            Double saldoActual = cuentaSnapshot.getDouble("saldo");
            if (saldoActual == null) {
                throw new RuntimeException("El saldo de la cuenta no está definido.");
            }

            Double nuevoSaldo = saldoActual;
            if (transaccion.getTipo().equalsIgnoreCase("ingreso")) {
                nuevoSaldo += transaccion.getImporte();
            } else if (transaccion.getTipo().equalsIgnoreCase("egreso")) {
                nuevoSaldo -= transaccion.getImporte();
            } else {
                throw new RuntimeException("El tipo de transacción no es válido.");
            }

            transaction.update(cuentaRef, "saldo", nuevoSaldo);

            DocumentReference nuevaTransaccionRef = transaccionesRef.document();
            transaction.set(nuevaTransaccionRef, transaccion);

            return nuevaTransaccionRef.getId();
        });

        return future.get();
    }

    public String newTransaccionTarjeta(String spaceId, Transaccion transaccion)
            throws ExecutionException, InterruptedException {

        CollectionReference transaccionesRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME);
        DocumentReference tarjetaRef = firestore.collection("spaces").document(spaceId).collection("tarjetas")
                .document(transaccion.getTarjetaId());

        ApiFuture<String> future = firestore.runTransaction(transaction -> {
            DocumentSnapshot tarjetaSnapshot = transaction.get(tarjetaRef).get();

            if (!tarjetaSnapshot.exists()) {
                throw new RuntimeException("La tarjeta especificada no existe.");
            }

            Double saldoActual = tarjetaSnapshot.getDouble("saldo");
            if (saldoActual == null) {
                throw new RuntimeException("El saldo de la tarjeta no está definido.");
            }

            Double nuevoSaldo = saldoActual;
            if (transaccion.getTipo().equalsIgnoreCase("ingreso")) {
                nuevoSaldo -= transaccion.getImporte();
            } else if (transaccion.getTipo().equalsIgnoreCase("egreso")) {
                nuevoSaldo += transaccion.getImporte();
            } else {
                throw new RuntimeException("El tipo de transacción no es válido.");
            }

            transaction.update(tarjetaRef, "saldo", nuevoSaldo);

            DocumentReference nuevaTransaccionRef = transaccionesRef.document();
            transaction.set(nuevaTransaccionRef, transaccion);

            return nuevaTransaccionRef.getId();
        });

        return future.get();
    }

    public ArrayList<TransaccionDto> getTransaccionesCuentaByMonth(String spaceId, String yearMonth, String cuentaId)
            throws ExecutionException, InterruptedException {

        String fechaInicio = yearMonth + "-01";
        String fechaFin = yearMonth + "-31";

        CollectionReference transaccionesRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME);
        CollectionReference transferenciasRef = firestore.collection("spaces").document(spaceId).collection("transferencias");

        // Fire all 3 queries in parallel
        ApiFuture<QuerySnapshot> transaccionesFuture = transaccionesRef
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin)
                .whereEqualTo("cuentaId", cuentaId)
                .get();
        ApiFuture<QuerySnapshot> transferenciasSalidaFuture = transferenciasRef
                .whereEqualTo("cuentaOrigenId", cuentaId)
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin)
                .get();
        ApiFuture<QuerySnapshot> transferenciasEntradaFuture = transferenciasRef
                .whereEqualTo("cuentaDestinoId", cuentaId)
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin)
                .get();

        java.util.List<QueryDocumentSnapshot> transaccionesDocs = transaccionesFuture.get().getDocuments();
        java.util.List<QueryDocumentSnapshot> transferenciasSalida = transferenciasSalidaFuture.get().getDocuments();
        java.util.List<QueryDocumentSnapshot> transferenciasEntrada = transferenciasEntradaFuture.get().getDocuments();

        ArrayList<TransaccionDto> result = new ArrayList<>();

        for (QueryDocumentSnapshot doc : transaccionesDocs) {
            TransaccionDto t = doc.toObject(TransaccionDto.class);
            t.setId(doc.getId());
            result.add(t);
        }

        for (QueryDocumentSnapshot doc : transferenciasSalida) {
            TransaccionDto t = new TransaccionDto();
            t.setId(doc.getId());
            t.setFecha(doc.getString("fecha"));
            t.setImporte(doc.getDouble("importe"));
            t.setCuentaId(doc.getString("cuentaOrigenId"));
            t.setTipo("Egreso");
            t.setConcepto(doc.getString("concepto"));
            t.setTransferencia(true);

            String tipo = doc.getString("tipoCuentaDestino");
            String nombreDestino = doc.getString("nombreCuentaDestino");
            if (nombreDestino == null) nombreDestino = "";
            if ("Cuenta".equals(tipo)) {
                t.setDescripcion("Transferencia a " + nombreDestino);
            } else if ("Tarjeta".equals(tipo)) {
                t.setDescripcion("Pago a " + nombreDestino);
            } else {
                t.setDescripcion("Transferencia a cuenta");
            }
            result.add(t);
        }

        for (QueryDocumentSnapshot doc : transferenciasEntrada) {
            TransaccionDto t = new TransaccionDto();
            t.setId(doc.getId());
            t.setFecha(doc.getString("fecha"));
            t.setImporte(doc.getDouble("importe"));
            t.setCuentaId(doc.getString("cuentaOrigenId"));
            t.setTipo("Ingreso");
            t.setConcepto(doc.getString("concepto"));
            t.setTransferencia(true);

            String nombreOrigen = doc.getString("nombreCuentaOrigen");
            if (nombreOrigen == null) nombreOrigen = "";
            t.setDescripcion("Transferencia de " + nombreOrigen);
            result.add(t);
        }

        return result;
    }

    public ArrayList<TransaccionDto> getTransaccionesTarjetaByMonth(String spaceId, String yearMonth, String tarjetaId)
            throws ExecutionException, InterruptedException {

        String fechaInicio = yearMonth + "-01";
        String fechaFin = yearMonth + "-31";

        ApiFuture<QuerySnapshot> querySnapshot = firestore.collection("spaces").document(spaceId)
                .collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin)
                .whereEqualTo("tarjetaId", tarjetaId)
                .get();

        ArrayList<TransaccionDto> result = new ArrayList<>();
        for (QueryDocumentSnapshot doc : querySnapshot.get().getDocuments()) {
            TransaccionDto t = doc.toObject(TransaccionDto.class);
            t.setId(doc.getId());
            result.add(t);
        }
        return result;
    }

    public ArrayList<TransaccionDto> getTransaccionesTarjetaByCut(String spaceId, String fechaInicio, String fechaFin, String tarjetaId)
            throws ExecutionException, InterruptedException {

        CollectionReference transaccionesRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME);
        CollectionReference transferenciasRef = firestore.collection("spaces").document(spaceId).collection("transferencias");

        // Fire both queries in parallel
        ApiFuture<QuerySnapshot> transaccionesFuture = transaccionesRef
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin)
                .whereEqualTo("tarjetaId", tarjetaId)
                .get();
        ApiFuture<QuerySnapshot> transferenciasFuture = transferenciasRef
                .whereEqualTo("cuentaDestinoId", tarjetaId)
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin)
                .get();

        java.util.List<QueryDocumentSnapshot> transaccionesDocs = transaccionesFuture.get().getDocuments();
        java.util.List<QueryDocumentSnapshot> transferenciasDocs = transferenciasFuture.get().getDocuments();

        ArrayList<TransaccionDto> result = new ArrayList<>();

        for (QueryDocumentSnapshot doc : transaccionesDocs) {
            TransaccionDto t = doc.toObject(TransaccionDto.class);
            t.setId(doc.getId());
            result.add(t);
        }

        for (QueryDocumentSnapshot doc : transferenciasDocs) {
            TransaccionDto t = new TransaccionDto();
            t.setId(doc.getId());
            t.setFecha(doc.getString("fecha"));
            t.setImporte(doc.getDouble("importe"));
            t.setCuentaId(doc.getString("cuentaOrigenId"));
            t.setTipo("Ingreso");
            t.setConcepto(doc.getString("concepto"));
            t.setTransferencia(true);

            String nombreOrigen = doc.getString("nombreCuentaOrigen");
            if (nombreOrigen == null) nombreOrigen = "";
            t.setDescripcion("Transferencia de " + nombreOrigen);
            result.add(t);
        }

        return result;
    }

    public Boolean getExistTransaccionesByTarjeta(String spaceId, String tarjetaId)
            throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection("spaces").document(spaceId)
                .collection(COLLECTION_NAME)
                .whereEqualTo("tarjetaId", tarjetaId)
                .limit(1)
                .get().get();
        return !snapshot.isEmpty();
    }

    public Boolean getExistTransaccionesByCuenta(String spaceId, String cuentaId)
            throws ExecutionException, InterruptedException {
        QuerySnapshot snapshot = firestore.collection("spaces").document(spaceId)
                .collection(COLLECTION_NAME)
                .whereEqualTo("cuentaId", cuentaId)
                .limit(1)
                .get().get();
        return !snapshot.isEmpty();
    }

    public TransaccionDto getTransaccionById(String spaceId, String transaccionId)
            throws ExecutionException, InterruptedException {
        DocumentSnapshot document = firestore.collection("spaces").document(spaceId)
                .collection(COLLECTION_NAME).document(transaccionId).get().get();
        if (!document.exists()) return null;
        TransaccionDto transaccion = document.toObject(TransaccionDto.class);
        transaccion.setId(document.getId());
        return transaccion;
    }

    public void updateTransaccion(String spaceId, String transaccionId, Transaccion transaccion)
            throws ExecutionException, InterruptedException {
        firestore.collection("spaces").document(spaceId)
                .collection(COLLECTION_NAME).document(transaccionId)
                .set(transaccion).get();
    }

    public void deleteTransaccion(String spaceId, String transaccionId) throws ExecutionException, InterruptedException {

        DocumentReference transaccionRef = firestore.collection("spaces").document(spaceId)
                .collection(COLLECTION_NAME).document(transaccionId);
        CollectionReference cuentasRef = firestore.collection("spaces").document(spaceId).collection("cuentas");
        CollectionReference tarjetasRef = firestore.collection("spaces").document(spaceId).collection("tarjetas");

        firestore.runTransaction(transaction -> {
            DocumentSnapshot transaccionSnapshot = transaction.get(transaccionRef).get();

            if (!transaccionSnapshot.exists()) {
                throw new RuntimeException("La transacción especificada no existe.");
            }
            Transaccion transaccionNullable = transaccionSnapshot.toObject(Transaccion.class);
            if (transaccionNullable == null) {
                throw new RuntimeException("Error al obtener los datos de la transacción.");
            }
            final Transaccion transaccion = transaccionNullable;

            if (transaccion.getCuentaId() != null && !transaccion.getCuentaId().isEmpty()) {
                DocumentReference cuentaRef = cuentasRef.document(transaccion.getCuentaId());
                DocumentSnapshot cuentaSnapshot = transaction.get(cuentaRef).get();

                if (!cuentaSnapshot.exists()) {
                    throw new RuntimeException("La cuenta asociada a la transacción no existe.");
                }

                Double saldoActual = cuentaSnapshot.getDouble("saldo");
                if (saldoActual == null) saldoActual = 0.0;
                Double nuevoSaldo = saldoActual;
                if (transaccion.getTipo().equalsIgnoreCase("ingreso")) {
                    nuevoSaldo -= transaccion.getImporte();
                } else if (transaccion.getTipo().equalsIgnoreCase("egreso")) {
                    nuevoSaldo += transaccion.getImporte();
                }
                transaction.update(cuentaRef, "saldo", nuevoSaldo);

            } else if (transaccion.getTarjetaId() != null && !transaccion.getTarjetaId().isEmpty()) {
                DocumentReference tarjetaRef = tarjetasRef.document(transaccion.getTarjetaId());
                DocumentSnapshot tarjetaSnapshot = transaction.get(tarjetaRef).get();

                if (!tarjetaSnapshot.exists()) {
                    throw new RuntimeException("La tarjeta asociada a la transacción no existe.");
                }

                Double saldoActual = tarjetaSnapshot.getDouble("saldo");
                if (saldoActual == null) saldoActual = 0.0;
                Double nuevoSaldo = saldoActual;
                if (transaccion.getTipo().equalsIgnoreCase("ingreso")) {
                    nuevoSaldo += transaccion.getImporte();
                } else if (transaccion.getTipo().equalsIgnoreCase("egreso")) {
                    nuevoSaldo -= transaccion.getImporte();
                }
                transaction.update(tarjetaRef, "saldo", nuevoSaldo);

            } else {
                throw new RuntimeException("La transacción no está asociada a una cuenta o tarjeta válida.");
            }

            transaction.delete(transaccionRef);
            return null;
        }).get();
    }
}

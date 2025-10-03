package com.finanzas.app_back.repositories;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.finanzas.app_back.dto.Transacciones.TransaccionDto;
import com.finanzas.app_back.model.Tarjeta;
import com.finanzas.app_back.model.Transaccion;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@Repository
public class TransaccionesRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "transacciones";

    public String newTransaccionCuenta(String uid, Transaccion transaccion)
            throws ExecutionException, InterruptedException {

        System.out.println("Transaccion a registrar: " + transaccion);

        CollectionReference transaccionesRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME);
        DocumentReference cuentaRef = firestore.collection("users").document(uid).collection("cuentas")
                .document(transaccion.getCuentaId());

        // Ejecutar la transacción
        ApiFuture<String> future = firestore.runTransaction(transaction -> {
            // Leer el saldo actual de la cuenta
            ApiFuture<DocumentSnapshot> cuentaSnapshotFuture = transaction.get(cuentaRef);
            DocumentSnapshot cuentaSnapshot = cuentaSnapshotFuture.get(); // Obtener el DocumentSnapshot

            if (!cuentaSnapshot.exists()) {
                throw new RuntimeException("La cuenta especificada no existe.");
            }

            // Obtener el saldo actual
            Double saldoActual = cuentaSnapshot.getDouble("saldo");
            if (saldoActual == null) {
                throw new RuntimeException("El saldo de la cuenta no está definido.");
            }

            // Calcular el nuevo saldo
            Double nuevoSaldo = saldoActual;
            if (transaccion.getTipo().equalsIgnoreCase("ingreso")) {
                nuevoSaldo += transaccion.getImporte();
            } else if (transaccion.getTipo().equalsIgnoreCase("egreso")) {
                nuevoSaldo -= transaccion.getImporte();
            } else {
                throw new RuntimeException("El tipo de transacción no es válido.");
            }

            // Actualizar el saldo de la cuenta
            transaction.update(cuentaRef, "saldo", nuevoSaldo);

            // Crear el registro de la transacción
            DocumentReference nuevaTransaccionRef = transaccionesRef.document();
            transaction.set(nuevaTransaccionRef, transaccion);

            return nuevaTransaccionRef.getId(); // Retornar el ID de la nueva transacción
        });

        // Bloquear hasta que la transacción se complete y obtener el resultado
        return future.get();
    }

    public String newTransaccionTarjeta(String uid, Transaccion transaccion)
            throws ExecutionException, InterruptedException {

        System.out.println("Transaccion a registrar: " + transaccion);

        CollectionReference transaccionesRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME);
        DocumentReference tarjetaRef = firestore.collection("users").document(uid).collection("tarjetas")
                .document(transaccion.getTarjetaId());

        // Ejecutar la transacción
        ApiFuture<String> future = firestore.runTransaction(transaction -> {

            ApiFuture<DocumentSnapshot> tarjetaSnapshotFuture = transaction.get(tarjetaRef);
            DocumentSnapshot tarjetaSnapshot = tarjetaSnapshotFuture.get(); // Obtener el DocumentSnapshot

            if (!tarjetaSnapshot.exists()) {
                throw new RuntimeException("La tarjeta especificada no existe.");
            }

            // Obtener el saldo actual
            Double saldoActual = tarjetaSnapshot.getDouble("saldo");

            // Calcular el nuevo saldo
            Double nuevoSaldo = saldoActual;
            System.out.println("Saldo actual tarjeta al crear: " + saldoActual);
            if (transaccion.getTipo().equalsIgnoreCase("ingreso")) {
                nuevoSaldo -= transaccion.getImporte();
            } else if (transaccion.getTipo().equalsIgnoreCase("egreso")) {
                nuevoSaldo += transaccion.getImporte();
            } else {
                throw new RuntimeException("El tipo de transacción no es válido.");
            }

            System.out.println("Saldo nuevo tarjeta al crear: " + nuevoSaldo);

            // Actualizar el saldo de la cuenta
            transaction.update(tarjetaRef, "saldo", nuevoSaldo);

            // Crear el registro de la transacción
            DocumentReference nuevaTransaccionRef = transaccionesRef.document();
            transaction.set(nuevaTransaccionRef, transaccion);

            return nuevaTransaccionRef.getId(); // Retornar el ID de la nueva transacción
        });

        // Bloquear hasta que la transacción se complete y obtener el resultado
        return future.get();
    }

    public ArrayList<TransaccionDto> getTransaccionesCuentaByMonth(String uid, String yearMonth, String cuentaId)
            throws ExecutionException, InterruptedException {
        CollectionReference transaccionesRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME);

        // yearMonth en formato "yyyy-MM", por ejemplo "2025-10"
        System.out.println("Filtrando transacciones para el mes: " + yearMonth + " y cuentaId: " + cuentaId);

        // Construir el rango de fechas basado en el año y mes
        String fechaInicio = yearMonth + "-01"; // Ejemplo: "2025-10-01"
        String fechaFin = yearMonth + "-31"; // Ejemplo: "2025-10-31"

        ApiFuture<QuerySnapshot> querySnapshot = transaccionesRef
                .whereGreaterThanOrEqualTo("fecha", fechaInicio) // Fecha >= fechaInicio
                .whereLessThanOrEqualTo("fecha", fechaFin) // Fecha <= fechaFin
                .whereEqualTo("cuentaId", cuentaId)
                .get();

        ArrayList<TransaccionDto> transaccionesList = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
            TransaccionDto transaccion = document.toObject(TransaccionDto.class);
            transaccion.setId(document.getId()); // Asigna el ID del documento a la transaccion

            transaccionesList.add(transaccion);
        }

        return transaccionesList;
    }

    public ArrayList<TransaccionDto> getTransaccionesTarjetaByMonth(String uid, String yearMonth, String TarjetaId)
            throws ExecutionException, InterruptedException {
        CollectionReference transaccionesRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME);

        // yearMonth en formato "yyyy-MM", por ejemplo "2025-10"

        // Construir el rango de fechas basado en el año y mes
        String fechaInicio = yearMonth + "-01"; // Ejemplo: "2025-10-01"
        String fechaFin = yearMonth + "-31"; // Ejemplo: "2025-10-31"

        ApiFuture<QuerySnapshot> querySnapshot = transaccionesRef
                .whereGreaterThanOrEqualTo("fecha", fechaInicio) // Fecha >= fechaInicio
                .whereLessThanOrEqualTo("fecha", fechaFin) // Fecha <= fechaFin
                .whereEqualTo("tarjetaId", TarjetaId)
                .get();

        ArrayList<TransaccionDto> transaccionesList = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
            TransaccionDto transaccion = document.toObject(TransaccionDto.class);
            transaccion.setId(document.getId()); // Asigna el ID del documento a la transaccion

            transaccionesList.add(transaccion);
        }

        return transaccionesList;
    }

    public TransaccionDto getTransaccionById(String uid, String transaccionId)
            throws ExecutionException, InterruptedException {
        DocumentReference transaccionRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME)
                .document(transaccionId);
        ApiFuture<DocumentSnapshot> future = transaccionRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            TransaccionDto transaccion = document.toObject(TransaccionDto.class);
            transaccion.setId(document.getId()); // Asigna el ID del documento a la transaccion
            return transaccion;
        } else {
            return null; // O lanza una excepción si prefieres
        }
    }

   public void updateTransaccion(String uid, String transaccionId, Transaccion transaccion) throws ExecutionException, InterruptedException {
        DocumentReference transaccionRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME).document(transaccionId);
        ApiFuture<WriteResult> writeResult = transaccionRef.set(transaccion);
        writeResult.get(); // Espera a que la operación se complete
    }








    public void deleteTransaccion(String uid, String transaccionId) throws ExecutionException, InterruptedException {

        DocumentReference transaccionRef = firestore.collection("users").document(uid).collection(COLLECTION_NAME)
                .document(transaccionId);
        CollectionReference cuentasRef = firestore.collection("users").document(uid).collection("cuentas");
        CollectionReference terjetasRef = firestore.collection("users").document(uid).collection("tarjetas");

        ApiFuture<String> future = firestore.runTransaction(transaction -> {
            // Leer la transacción a eliminar
            ApiFuture<DocumentSnapshot> transaccionSnapshotFuture = transaction.get(transaccionRef);
            DocumentSnapshot transaccionSnapshot = transaccionSnapshotFuture.get(); // Obtener el DocumentSnapshot

            if (!transaccionSnapshot.exists()) {
                throw new RuntimeException("La transacción especificada no existe.");
            }
            Transaccion transaccion = transaccionSnapshot.toObject(Transaccion.class);
            if (transaccion == null) {
                throw new RuntimeException("Error al obtener los datos de la transacción.");
            }

            if (transaccion.getCuentaId() != null && !transaccion.getCuentaId().isEmpty()) {
                // Leer el saldo actual de la cuenta asociada
                DocumentReference cuentaRef = cuentasRef.document(transaccion.getCuentaId());
                ApiFuture<DocumentSnapshot> cuentaSnapshotFuture = transaction.get(cuentaRef);
                DocumentSnapshot cuentaSnapshot = cuentaSnapshotFuture.get(); // Obtener el DocumentSnapshot

                if (!cuentaSnapshot.exists()) {
                    throw new RuntimeException("La cuenta asociada a la transacción no existe.");
                }

                // Obtener el saldo actual
                Double saldoActual = cuentaSnapshot.getDouble("saldo");

                // Calcular el nuevo saldo revirtiendo la transacción
                Double nuevoSaldo = saldoActual;
                if (transaccion.getTipo().equalsIgnoreCase("ingreso")) {
                    nuevoSaldo -= transaccion.getImporte();
                } else if (transaccion.getTipo().equalsIgnoreCase("egreso")) {
                    nuevoSaldo += transaccion.getImporte();
                }

                cuentaRef.update("saldo", nuevoSaldo);
            } else if (transaccion.getTarjetaId() != null && !transaccion.getTarjetaId().isEmpty()) {
                // Leer el saldo actual de la tarjeta asociada
                DocumentReference tarjetaRef = terjetasRef.document(transaccion.getTarjetaId());
                ApiFuture<DocumentSnapshot> tarjetaSnapshotFuture = transaction.get(tarjetaRef);
                DocumentSnapshot tarjetaSnapshot = tarjetaSnapshotFuture.get(); // Obtener el DocumentSnapshot

                if (!tarjetaSnapshot.exists()) {
                    throw new RuntimeException("La tarjeta asociada a la transacción no existe.");
                }

                // Obtener el saldo actual
                Double saldoActual = tarjetaSnapshot.getDouble("saldo");

                System.out.println("Saldo actual tarjeta al eliminar: " + saldoActual);

                // Calcular el nuevo saldo revirtiendo la transacción
                Double nuevoSaldo = saldoActual;
                if (transaccion.getTipo().equalsIgnoreCase("ingreso")) {
                    nuevoSaldo += transaccion.getImporte();
                } else if (transaccion.getTipo().equalsIgnoreCase("egreso")) {
                    nuevoSaldo -= transaccion.getImporte();
                }

                System.out.println("Nuevo saldo tarjeta al eliminar: " + nuevoSaldo);

                tarjetaRef.update("saldo", nuevoSaldo);
            } else {
                throw new RuntimeException("La transacción no está asociada a una cuenta o tarjeta válida.");
            }

            // Eliminar la transacción
            transaccionRef.delete();

            return "Transacción eliminada exitosamente.";

        });

    }

}

package com.finanzas.app_back.repositories;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.finanzas.app_back.dto.Cuentas.CuentaDto;
import com.finanzas.app_back.dto.Tarjetas.TarjetaDto;
import com.finanzas.app_back.dto.Transferencias.TransferenciaDto;
import com.finanzas.app_back.model.Transferencia;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

@Repository
public class TransferenciasRepository {

    @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "transferencias";

    public String newTransferenciaCuenta(String spaceId, Transferencia transferencia, CuentaDto cuentaOrigen, CuentaDto cuentaDestino) throws ExecutionException, InterruptedException {

        CollectionReference transaccionesRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME);
        DocumentReference cuentaOrigenRef = firestore.collection("spaces").document(spaceId).collection("cuentas").document(cuentaOrigen.getId());
        DocumentReference cuentaDestinoRef = firestore.collection("spaces").document(spaceId).collection("cuentas").document(cuentaDestino.getId());

        ApiFuture<String> future = firestore.runTransaction(transaction -> {

            ApiFuture<DocumentSnapshot> cuentaOrigenSnapshotFuture = transaction.get(cuentaOrigenRef);
            DocumentSnapshot cuentaOrigenSnapshot = cuentaOrigenSnapshotFuture.get();

            if (!cuentaOrigenSnapshot.exists()) {
                throw new RuntimeException("La cuenta origen especificada no existe.");
            }

            ApiFuture<DocumentSnapshot> cuentaDestinoSnapshotFuture = transaction.get(cuentaDestinoRef);
            DocumentSnapshot cuentaDestinoSnapshot = cuentaDestinoSnapshotFuture.get();
            if (!cuentaDestinoSnapshot.exists()) {
                throw new RuntimeException("La cuenta destino especificada no existe.");
            }

            double nuevoSaldoOrigen = cuentaOrigen.getSaldo() - transferencia.getImporte();
            transaction.update(cuentaOrigenRef, "saldo", nuevoSaldoOrigen);

            double nuevoSaldoDestino = cuentaDestino.getSaldo() + transferencia.getImporte();
            transaction.update(cuentaDestinoRef, "saldo", nuevoSaldoDestino);

            DocumentReference newTransaccionRef = transaccionesRef.document();
            transaction.set(newTransaccionRef, transferencia);

            return newTransaccionRef.getId();
        });

        return future.get();
    }

    public String newTransferenciaTarjeta(String spaceId, Transferencia transferencia, CuentaDto cuentaOrigen, TarjetaDto tarjetaDestino) throws ExecutionException, InterruptedException {

        CollectionReference transaccionesRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME);
        DocumentReference cuentaOrigenRef = firestore.collection("spaces").document(spaceId).collection("cuentas").document(cuentaOrigen.getId());
        DocumentReference tarjetaDestinoRef = firestore.collection("spaces").document(spaceId).collection("tarjetas").document(tarjetaDestino.getId());

        ApiFuture<String> future = firestore.runTransaction(transaction -> {

            ApiFuture<DocumentSnapshot> cuentaOrigenSnapshotFuture = transaction.get(cuentaOrigenRef);
            DocumentSnapshot cuentaOrigenSnapshot = cuentaOrigenSnapshotFuture.get();

            if (!cuentaOrigenSnapshot.exists()) {
                throw new RuntimeException("La cuenta origen especificada no existe.");
            }

            ApiFuture<DocumentSnapshot> tarjetaDestinoSnapshotFuture = transaction.get(tarjetaDestinoRef);
            DocumentSnapshot tarjetaDestinoSnapshot = tarjetaDestinoSnapshotFuture.get();
            if (!tarjetaDestinoSnapshot.exists()) {
                throw new RuntimeException("La tarjeta destino especificada no existe.");
            }

            double nuevoSaldoOrigen = cuentaOrigen.getSaldo() - transferencia.getImporte();
            transaction.update(cuentaOrigenRef, "saldo", nuevoSaldoOrigen);

            double nuevoSaldoDestino = tarjetaDestino.getSaldo() - transferencia.getImporte();
            transaction.update(tarjetaDestinoRef, "saldo", nuevoSaldoDestino);

            DocumentReference newTransaccionRef = transaccionesRef.document();
            transaction.set(newTransaccionRef, transferencia);

            return newTransaccionRef.getId();
        });

        return future.get();
    }

    public TransferenciaDto getTransferenciaById(String spaceId, String transferenciaId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(transferenciaId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            Transferencia transferencia = document.toObject(Transferencia.class);
            if (transferencia != null) {
                TransferenciaDto transferenciaDto = document.toObject(TransferenciaDto.class);
                transferenciaDto.setId(document.getId());
                return transferenciaDto;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void deleteTransferencia(String spaceId, String transferenciaId) throws ExecutionException, InterruptedException {

        DocumentReference transferenciaRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(transferenciaId);
        CollectionReference cuentasRef = firestore.collection("spaces").document(spaceId).collection("cuentas");
        CollectionReference terjetasRef = firestore.collection("spaces").document(spaceId).collection("tarjetas");

        ApiFuture<String> future = firestore.runTransaction(transaction -> {

            ApiFuture<DocumentSnapshot> transferenciaSnapshotFuture = transaction.get(transferenciaRef);
            DocumentSnapshot transferenciaSnapshot = transferenciaSnapshotFuture.get();

            if (!transferenciaSnapshot.exists()) {
                throw new RuntimeException("La transferencia especificada no existe.");
            }

            Transferencia transferencia = transferenciaSnapshot.toObject(Transferencia.class);
            if (transferencia == null) {
                throw new RuntimeException("Error al convertir la transferencia.");
            }

            DocumentReference cuentaOrigenRef = cuentasRef.document(transferencia.getCuentaOrigenId());
            ApiFuture<DocumentSnapshot> cuentaOrigenSnapshotFuture = transaction.get(cuentaOrigenRef);
            DocumentSnapshot cuentaOrigenSnapshot = cuentaOrigenSnapshotFuture.get();

            if (!cuentaOrigenSnapshot.exists()) {
                throw new RuntimeException("La cuenta origen especificada no existe.");
            }

            double nuevoSaldoOrigen = cuentaOrigenSnapshot.getDouble("saldo") + transferencia.getImporte();

            if (transferencia.getTipoCuentaDestino().equalsIgnoreCase("Cuenta")) {

                try {
                    DocumentReference cuentaDestinoRef = cuentasRef.document(transferencia.getCuentaDestinoId());
                    ApiFuture<DocumentSnapshot> cuentaDestinoSnapshotFuture = transaction.get(cuentaDestinoRef);
                    DocumentSnapshot cuentaDestinoSnapshot = cuentaDestinoSnapshotFuture.get();

                    if (!cuentaDestinoSnapshot.exists()) {
                        throw new RuntimeException("La cuenta destino especificada no existe.");
                    }

                    double nuevoSaldoDestino = cuentaDestinoSnapshot.getDouble("saldo") - transferencia.getImporte();
                    transaction.update(cuentaDestinoRef, "saldo", nuevoSaldoDestino);
                } catch (Exception e) {
                    System.err.println("Error al obtener o actualizar la cuenta destino: " + e.getMessage());
                    throw e;
                }

            } else if (transferencia.getTipoCuentaDestino().equalsIgnoreCase("Tarjeta")) {

                try {
                    DocumentReference tarjetaDestinoRef = terjetasRef.document(transferencia.getCuentaDestinoId());
                    ApiFuture<DocumentSnapshot> tarjetaDestinoSnapshotFuture = transaction.get(tarjetaDestinoRef);
                    DocumentSnapshot tarjetaDestinoSnapshot = tarjetaDestinoSnapshotFuture.get();

                    if (!tarjetaDestinoSnapshot.exists()) {
                        throw new RuntimeException("La tarjeta destino especificada no existe.");
                    }

                    System.out.println("Tarjeta destino encontrada: " + tarjetaDestinoSnapshot.getId());

                    double nuevoSaldoDestino = tarjetaDestinoSnapshot.getDouble("saldo") + transferencia.getImporte();
                    transaction.update(tarjetaDestinoRef, "saldo", nuevoSaldoDestino);
                } catch (Exception e) {
                    System.err.println("Error al obtener o actualizar la tarjeta destino: " + e.getMessage());
                    throw e;
                }

            }

            try {
                transaction.update(cuentaOrigenRef, "saldo", nuevoSaldoOrigen);
                transaction.delete(transferenciaRef);
            } catch (Exception e) {
                System.err.println("Error al actualizar el saldo de la cuenta origen o eliminar la transferencia: " + e.getMessage());
                throw e;
            }

            return "Transferencia eliminada y saldos actualizados.";
        });

    }


    public TransferenciaDto updateTransferencia(String spaceId, String transferenciaId, TransferenciaDto transferenciaData) {

            System.out.println("Actualizando transferencia ID: " + transferenciaId + " para space ID: " + spaceId);

        try {

            DocumentReference transferenciaRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(transferenciaId);
            CollectionReference cuentasRef = firestore.collection("spaces").document(spaceId).collection("cuentas");
            CollectionReference terjetasRef = firestore.collection("spaces").document(spaceId).collection("tarjetas");

            ApiFuture<TransferenciaDto> future = firestore.runTransaction(transaction -> {

                System.out.println("Iniciando transacción para actualizar transferencia...");

                DocumentReference cuentaOrigenRef = null;
                double nuevoSaldoOrigen = 0;
                Boolean actualizarCuentaOrigen = false;

                DocumentReference nuevaCuentaOrigenRef = null;
                double nuevoSaldoNuevaOrigen = 0;
                Boolean actualizarNuevaCuentaOrigen = false;

                DocumentReference cuentaDestinoRef = null;
                double nuevoSaldoDestino = 0;
                Boolean actualizarCuentaDestino = false;

                DocumentReference nuevaCuentaDestinoRef = null;
                double nuevoSaldoNuevaDestino = 0;
                Boolean actualizarNuevaCuentaDestino = false;

                DocumentReference tarjetaDestinoRef = null;
                double nuevoSaldoTarjetaDestino = 0;
                Boolean actualizarTarjetaDestino = false;

                DocumentReference nuevaTarjetaDestinoRef = null;
                double nuevoSaldoNuevaTarjetaDestino = 0;
                Boolean actualizarNuevaTarjetaDestino = false;

                ApiFuture<DocumentSnapshot> transferenciaSnapshotFuture = transaction.get(transferenciaRef);
                DocumentSnapshot transferenciaSnapshot = transferenciaSnapshotFuture.get();

                if (!transferenciaSnapshot.exists()) {
                    throw new RuntimeException("La transferencia especificada no existe.");
                }

                TransferenciaDto transferenciaOriginalDto = transferenciaSnapshot.toObject(TransferenciaDto.class);
                if (transferenciaOriginalDto == null) {
                    throw new RuntimeException("Error al convertir la transferencia.");
                }

                System.out.println("Cuenta Origen Original ID: " + transferenciaOriginalDto.getCuentaOrigenId());

                if (!transferenciaOriginalDto.getCuentaOrigenId().equals(transferenciaData.getCuentaOrigenId())) {

                    System.out.println("Cuenta Origen Nueva ID: " + transferenciaData.getCuentaOrigenId());

                    cuentaOrigenRef = cuentasRef.document(transferenciaOriginalDto.getCuentaOrigenId());
                    ApiFuture<DocumentSnapshot> cuentaOrigenSnapshotFuture = transaction.get(cuentaOrigenRef);
                    DocumentSnapshot cuentaOrigenSnapshot = cuentaOrigenSnapshotFuture.get();

                    if (!cuentaOrigenSnapshot.exists()) {
                        throw new RuntimeException("La cuenta origen especificada no existe.");
                    }
                    nuevoSaldoOrigen = cuentaOrigenSnapshot.getDouble("saldo") + transferenciaOriginalDto.getImporte();
                    actualizarCuentaOrigen = true;

                    nuevaCuentaOrigenRef = cuentasRef.document(transferenciaData.getCuentaOrigenId());
                    ApiFuture<DocumentSnapshot> nuevaCuentaOrigenSnapshotFuture = transaction.get(nuevaCuentaOrigenRef);
                    DocumentSnapshot nuevaCuentaOrigenSnapshot = nuevaCuentaOrigenSnapshotFuture.get();

                    if (!nuevaCuentaOrigenSnapshot.exists()) {
                        throw new RuntimeException("La nueva cuenta origen especificada no existe.");
                    }
                    nuevoSaldoNuevaOrigen = nuevaCuentaOrigenSnapshot.getDouble("saldo") - transferenciaData.getImporte();
                    actualizarNuevaCuentaOrigen = true;

                } else {

                    System.out.println("Cuenta Origen No Cambia ID: " + transferenciaData.getCuentaOrigenId());

                    if (!transferenciaOriginalDto.getImporte().equals(transferenciaData.getImporte())) {

                        cuentaOrigenRef = cuentasRef.document(transferenciaOriginalDto.getCuentaOrigenId());
                        ApiFuture<DocumentSnapshot> cuentaOrigenSnapshotFuture = transaction.get(cuentaOrigenRef);
                        DocumentSnapshot cuentaOrigenSnapshot = cuentaOrigenSnapshotFuture.get();

                        if (!cuentaOrigenSnapshot.exists()) {
                            throw new RuntimeException("La cuenta origen especificada no existe.");
                        }

                        double diferenciaImporte = transferenciaData.getImporte() - transferenciaOriginalDto.getImporte();
                        nuevoSaldoOrigen = cuentaOrigenSnapshot.getDouble("saldo") - diferenciaImporte;
                        actualizarCuentaOrigen = true;
                    }
                }

                System.out.println("Tipo Cuenta Destino Original: " + transferenciaOriginalDto.getTipoCuentaDestino());

                if (transferenciaOriginalDto.getTipoCuentaDestino().equalsIgnoreCase("Cuenta")) {

                    if (!transferenciaOriginalDto.getCuentaDestinoId().equals(transferenciaData.getCuentaDestinoId())) {

                        cuentaDestinoRef = cuentasRef.document(transferenciaOriginalDto.getCuentaDestinoId());
                        ApiFuture<DocumentSnapshot> cuentaDestinoSnapshotFuture = transaction.get(cuentaDestinoRef);
                        DocumentSnapshot cuentaDestinoSnapshot = cuentaDestinoSnapshotFuture.get();

                        if (!cuentaDestinoSnapshot.exists()) {
                            throw new RuntimeException("La cuenta destino especificada no existe.");
                        }

                        nuevoSaldoDestino = cuentaDestinoSnapshot.getDouble("saldo") - transferenciaOriginalDto.getImporte();
                        actualizarCuentaDestino = true;

                        nuevaCuentaDestinoRef = cuentasRef.document(transferenciaData.getCuentaDestinoId());
                        ApiFuture<DocumentSnapshot> nuevaCuentaDestinoSnapshotFuture = transaction.get(nuevaCuentaDestinoRef);
                        DocumentSnapshot nuevaCuentaDestinoSnapshot = nuevaCuentaDestinoSnapshotFuture.get();

                        if (!nuevaCuentaDestinoSnapshot.exists()) {
                            throw new RuntimeException("La nueva cuenta destino especificada no existe.");
                        }
                        nuevoSaldoNuevaDestino = nuevaCuentaDestinoSnapshot.getDouble("saldo") + transferenciaData.getImporte();
                        actualizarNuevaCuentaDestino = true;

                    } else {
                        if (!transferenciaOriginalDto.getImporte().equals(transferenciaData.getImporte())) {

                            cuentaDestinoRef = cuentasRef.document(transferenciaOriginalDto.getCuentaDestinoId());
                            ApiFuture<DocumentSnapshot> cuentaDestinoSnapshotFuture = transaction.get(cuentaDestinoRef);
                            DocumentSnapshot cuentaDestinoSnapshot = cuentaDestinoSnapshotFuture.get();

                            if (!cuentaDestinoSnapshot.exists()) {
                                throw new RuntimeException("La cuenta destino especificada no existe.");
                            }

                            double diferenciaImporte = transferenciaData.getImporte() - transferenciaOriginalDto.getImporte();
                            nuevoSaldoDestino = cuentaDestinoSnapshot.getDouble("saldo") + diferenciaImporte;
                            actualizarCuentaDestino = true;
                        }
                    }

                } else if (transferenciaOriginalDto.getTipoCuentaDestino().equalsIgnoreCase("Tarjeta")) {

                    if (!transferenciaOriginalDto.getCuentaDestinoId().equals(transferenciaData.getCuentaDestinoId())) {

                        tarjetaDestinoRef = terjetasRef.document(transferenciaOriginalDto.getCuentaDestinoId());
                        ApiFuture<DocumentSnapshot> tarjetaDestinoSnapshotFuture = transaction.get(tarjetaDestinoRef);
                        DocumentSnapshot tarjetaDestinoSnapshot = tarjetaDestinoSnapshotFuture.get();

                        if (!tarjetaDestinoSnapshot.exists()) {
                            throw new RuntimeException("La tarjeta destino especificada no existe.");
                        }

                        nuevoSaldoTarjetaDestino = tarjetaDestinoSnapshot.getDouble("saldo") + transferenciaOriginalDto.getImporte();
                        actualizarTarjetaDestino = true;

                        nuevaTarjetaDestinoRef = terjetasRef.document(transferenciaData.getCuentaDestinoId());
                        ApiFuture<DocumentSnapshot> nuevaTarjetaDestinoSnapshotFuture = transaction.get(nuevaTarjetaDestinoRef);
                        DocumentSnapshot nuevaTarjetaDestinoSnapshot = nuevaTarjetaDestinoSnapshotFuture.get();

                        if (!nuevaTarjetaDestinoSnapshot.exists()) {
                            throw new RuntimeException("La nueva tarjeta destino especificada no existe.");
                        }
                        nuevoSaldoNuevaTarjetaDestino = nuevaTarjetaDestinoSnapshot.getDouble("saldo") - transferenciaData.getImporte();
                        actualizarNuevaTarjetaDestino = true;

                    } else {
                        if (!transferenciaOriginalDto.getImporte().equals(transferenciaData.getImporte())) {

                            tarjetaDestinoRef = terjetasRef.document(transferenciaOriginalDto.getCuentaDestinoId());
                            ApiFuture<DocumentSnapshot> tarjetaDestinoSnapshotFuture = transaction.get(tarjetaDestinoRef);
                            DocumentSnapshot tarjetaDestinoSnapshot = tarjetaDestinoSnapshotFuture.get();

                            if (!tarjetaDestinoSnapshot.exists()) {
                                throw new RuntimeException("La tarjeta destino especificada no existe.");
                            }

                            double diferenciaImporte = transferenciaData.getImporte() - transferenciaOriginalDto.getImporte();
                            nuevoSaldoTarjetaDestino = tarjetaDestinoSnapshot.getDouble("saldo") - diferenciaImporte;
                            actualizarTarjetaDestino = true;
                        }
                    }
                }

                if (actualizarCuentaOrigen) {
                    transaction.update(cuentaOrigenRef, "saldo", nuevoSaldoOrigen);
                }
                if (actualizarNuevaCuentaOrigen) {
                    transaction.update(nuevaCuentaOrigenRef, "saldo", nuevoSaldoNuevaOrigen);
                }
                if (actualizarCuentaDestino) {
                    transaction.update(cuentaDestinoRef, "saldo", nuevoSaldoDestino);
                }
                if (actualizarNuevaCuentaDestino) {
                    transaction.update(nuevaCuentaDestinoRef, "saldo", nuevoSaldoNuevaDestino);
                }
                if (actualizarTarjetaDestino) {
                    transaction.update(tarjetaDestinoRef, "saldo", nuevoSaldoTarjetaDestino);
                }
                if (actualizarNuevaTarjetaDestino) {
                    transaction.update(nuevaTarjetaDestinoRef, "saldo", nuevoSaldoNuevaTarjetaDestino);
                }

                Transferencia updatedTransferencia = new Transferencia();
                updatedTransferencia.setFecha(transferenciaData.getFecha());
                updatedTransferencia.setImporte(transferenciaData.getImporte());
                updatedTransferencia.setCuentaOrigenId(transferenciaData.getCuentaOrigenId());
                updatedTransferencia.setTipoCuentaDestino(transferenciaData.getTipoCuentaDestino());
                updatedTransferencia.setCuentaDestinoId(transferenciaData.getCuentaDestinoId());
                updatedTransferencia.setConcepto(transferenciaData.getConcepto());

                transaction.set(transferenciaRef, updatedTransferencia);

                return transferenciaData;
            });

        } catch (Exception e) {
            System.err.println("Error al actualizar la transferencia: " + e.getMessage());
        }

        return null;

    }

}

package com.finanzas.app_back.service;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.springframework.stereotype.Service;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Cuentas.CuentaDto;
import com.finanzas.app_back.dto.Cuentas.CuentasList;
import com.finanzas.app_back.model.Cuenta;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@Service
public class CuentasService {

    public GenericResponse registrarCuenta(String uid ,CuentaDto dto) {
        GenericResponse response = new GenericResponse();

        try {

            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Crear una nueva entrada en la base de datos bajo el UID del usuario
            databaseReference.child("users").child(uid).child("cuentas").push().setValueAsync(dto);

            response.setCoderr("0000");
            response.setMessage("Cuenta registrada exitosamente.");
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al registrar la cuenta: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse obtenerCuentas(String uid) {
        GenericResponse response = new GenericResponse();
        ArrayList<Cuenta> cuentas = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        final Double[] saldoTotal = {0.0};
        

        try {

            
            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Referencia al nodo de las cuentas del usuario
            DatabaseReference cuentasRef = databaseReference.child("users").child(uid).child("cuentas");

            // Escuchar los datos de Firebase
            cuentasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot cuentaSnapshot : dataSnapshot.getChildren()) {
                        Cuenta cuenta = cuentaSnapshot.getValue(Cuenta.class);
                        String key = cuentaSnapshot.getKey();

                        if(cuenta != null){
                            cuenta.setId(key);
                            cuentas.add(cuenta);
                            saldoTotal[0] += cuenta.getSaldo();
                            
                        }

                        
                        
                        
                        System.out.println("Cuenta obtenida: " + cuenta);
                    }
                    latch.countDown(); // Liberar el latch cuando se complete la lectura
                }

                

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("Error al consultar las cuentas: " + databaseError.getMessage());
                    latch.countDown(); // Liberar el latch cuando se complete la lectura
                }
            });

            latch.await(); // Esperar a que se complete la operación asíncrona

            CuentasList cuentasList = new CuentasList();
            cuentasList.setCuentas(cuentas);
            cuentasList.setSaldoPagar(10000.50); // Inicializar en 0 o calcular según la lógica de tu aplicación
            cuentasList.setSaldoRestante(15000.00); // Inicializar en 0 o calcular según la lógica de tu aplicación
            cuentasList.setSaldoTotal(saldoTotal[0]);  


            response.setCoderr("0000");
            response.setMessage("Cuentas obtenidas exitosamente.");

            response.setData(cuentasList); // Asumiendo que 'cuentas' es la lista obtenida
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al obtener las cuentas: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse eliminarCuenta(String uid, String cuentaId) {
        GenericResponse response = new GenericResponse();

        try {
            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Referencia al nodo de la cuenta específica del usuario
            DatabaseReference cuentaRef = databaseReference.child("users").child(uid).child("cuentas").child(cuentaId);



            if(consultaCuenta(uid, cuentaId).getCoderr().equals("0001")){
                response.setCoderr("0001");
                response.setMessage("Cuenta no encontrada.");
                return response;
            }
            // Eliminar la cuenta
            cuentaRef.removeValueAsync();

            response.setCoderr("0000");
            response.setMessage("Cuenta eliminada exitosamente.");
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al eliminar la cuenta: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse actualizarCuenta(String uid, String cuentaId, CuentaDto updatedCuentaDto) {
        GenericResponse response = new GenericResponse();

        try {
            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Referencia al nodo de la cuenta específica del usuario
            DatabaseReference cuentaRef = databaseReference.child("users").child(uid).child("cuentas").child(cuentaId);

            if(consultaCuenta(uid, cuentaId).getCoderr().equals("0001")){
                response.setCoderr("0001");
                response.setMessage("Cuenta no encontrada.");
                return response;
            }

            // Actualizar los datos de la cuenta
            cuentaRef.setValueAsync(updatedCuentaDto);

            response.setCoderr("0000");
            response.setMessage("Cuenta actualizada exitosamente.");

            
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al actualizar la cuenta: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse consultaCuenta(String uid, String cuentaId) {
        GenericResponse response = new GenericResponse();
        CountDownLatch latch = new CountDownLatch(1);
        final Cuenta[] cuenta = new Cuenta[1];

        try {
            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Referencia al nodo de la cuenta específica del usuario
            DatabaseReference cuentaRef = databaseReference.child("users").child(uid).child("cuentas").child(cuentaId);

            // Escuchar los datos de Firebase
            cuentaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    cuenta[0] = dataSnapshot.getValue(Cuenta.class);
                    if (cuenta[0] != null) {
                        cuenta[0].setId(dataSnapshot.getKey());
                    }
                    latch.countDown(); // Liberar el latch cuando se complete la lectura
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("Error al consultar la cuenta: " + databaseError.getMessage());
                    latch.countDown(); // Liberar el latch cuando se complete la lectura
                }
            });

            latch.await(); // Esperar a que se complete la operación asíncrona

            if (cuenta[0] != null) {
                response.setCoderr("0000");
                response.setMessage("Cuenta obtenida exitosamente.");
                response.setData(cuenta[0]);
            } else {
                response.setCoderr("0001");
                response.setMessage("Cuenta no encontrada.");
            }
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al obtener la cuenta: " + e.getMessage());
        }

        return response;
    }


}

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
            DatabaseReference nuevaCuentaRef = databaseReference.child("users").child(uid).child("cuentas").push();
            String idGenerado = nuevaCuentaRef.getKey(); // Obtener el ID único generado
            nuevaCuentaRef.setValueAsync(dto);

            dto.setId(idGenerado);

            response.setCoderr("0000");
            response.setMessage("Cuenta registrada exitosamente.");
            response.setData(dto);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al registrar la cuenta: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse obtenerCuentas(String uid) {
        GenericResponse response = new GenericResponse();
        ArrayList<Cuenta> cuentas = new ArrayList<>();
        final Double[] saldoInvertido = {0.0};
        final Double[] saldoDisponible = {0.0};
        final Double[] saldoTotal = {0.0};
    
        try {

            cuentas = firebaseGetCuentas(uid);

            if (cuentas.isEmpty()) {
                response.setCoderr("0001");
                response.setMessage("No se encontraron cuentas.");
                return response;
            }
            
            for (Cuenta cuenta : cuentas) {

                if(cuenta.isActiva()){
                    if(cuenta.isInversion()){
                        saldoInvertido[0] += cuenta.getSaldo();
                    }
        
                    if(cuenta.isVista()){
                        saldoDisponible[0] += cuenta.getSaldo();
                    } 
    
                    saldoTotal[0] += cuenta.getSaldo();
                }
                
            }

            CuentasList cuentasList = new CuentasList();
            cuentasList.setCuentas(cuentas);
            cuentasList.setSaldoDisponible(saldoDisponible[0]); // Inicializar en 0 o calcular según la lógica de tu aplicación
            cuentasList.setSaldoInvertido(saldoInvertido[0]); // Inicializar en 0 o calcular según la lógica de tu aplicación
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
            
            Cuenta cuenta = firebaseGetCuentaById(uid, cuentaId);

            if(cuenta == null){
                response.setCoderr("0001");
                response.setMessage("Cuenta no encontrada.");
                return response;
            }

            if(cuenta.isActiva()){
                response.setCoderr("0002");
                response.setMessage("No se puede eliminar una cuenta activa.");
                return response;
            }   

            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Referencia al nodo de la cuenta específica del usuario
            DatabaseReference cuentaRef = databaseReference.child("users").child(uid).child("cuentas").child(cuentaId);

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

            Cuenta cuenta = firebaseGetCuentaById(uid, cuentaId);

            if(cuenta == null){
                response.setCoderr("0001");
                response.setMessage("Cuenta no encontrada.");
                return response;
            } 
            cuenta.setNombre(updatedCuentaDto.getNombre());
            cuenta.setDescripcion(updatedCuentaDto.getDescripcion());
            cuenta.setInstitucion(updatedCuentaDto.getInstitucion());
            cuenta.setInversion(updatedCuentaDto.isInversion());
            cuenta.setVista(updatedCuentaDto.isVista());

            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Referencia al nodo de la cuenta específica del usuario
            DatabaseReference cuentaRef = databaseReference.child("users").child(uid).child("cuentas").child(cuentaId);

            // Actualizar los datos de la cuenta
            cuentaRef.setValueAsync(cuenta);

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

        try {

            Cuenta cuenta = firebaseGetCuentaById(uid, cuentaId);

            if(cuenta == null){
                response.setCoderr("0001");
                response.setMessage("Cuenta no encontrada.");
                return response;
            }

            response.setCoderr("0000");
            response.setMessage("Cuenta obtenida exitosamente.");
            response.setData(cuenta);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al obtener la cuenta: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse ordenCuentas(String uid, ArrayList<CuentaDto> cuentas){

        GenericResponse response = new GenericResponse();

        CountDownLatch latch = new CountDownLatch(1);
        final Cuenta[] cuenta = new Cuenta[1];
        final int[] orden = {1};

        try {
            // Referencia a la base de datos de Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        

        // Recorrer el ArrayList de cuentas
        for (CuentaDto cuentaFor : cuentas) {
            
            if (cuentaFor.getId() != null) { // Asegurarse de que la cuenta tenga un ID válido
                // Referencia al nodo de la cuenta específica
                DatabaseReference cuentaRef = databaseReference.child("users").child(uid).child("cuentas").child(cuentaFor.getId());

                // Escuchar los datos de Firebase
                cuentaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        cuenta[0] = dataSnapshot.getValue(Cuenta.class);

                        System.out.println("Cuenta obtenida: " + cuenta[0].toString());

                        if (cuenta[0] != null) {
                            cuenta[0].setOrden(orden[0]);
                            orden[0]++;
                            // Actualizar los datos de la cuenta en Firebase
                            cuentaRef.setValueAsync(cuenta[0]);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("Error al consultar la cuenta: " + databaseError.getMessage());
                        latch.countDown(); // Liberar el latch cuando se complete la lectura
                    }
                });

                
            }
        }

        latch.countDown(); // Liberar el latch cuando se complete la lectura

        response.setCoderr("0000");
        response.setMessage("Cuentas actualizadas exitosamente.");
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al obtener la cuenta: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse activarCuenta(String uid, String cuentaId, boolean activa) {
        GenericResponse response = new GenericResponse();

        try {


            Cuenta cuenta = firebaseGetCuentaById(uid, cuentaId);

            if(cuenta == null){
                response.setCoderr("0001");
                response.setMessage("Cuenta no encontrada.");
                return response;
            }

            cuenta.setActiva(activa);


            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            // Referencia al nodo de la cuenta específica del usuario
            DatabaseReference cuentaRef = databaseReference.child("users").child(uid).child("cuentas").child(cuentaId);
            // Actualizar los datos de la cuenta
            cuentaRef.setValueAsync(cuenta);

            response.setCoderr("0000");
            response.setMessage("Cuenta actualizada exitosamente.");
            response.setData(activa);

            
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al actualizar la cuenta: " + e.getMessage());
        }

        return response;
    }



    public ArrayList<Cuenta> firebaseGetCuentas(String uid){
        ArrayList<Cuenta> cuentas = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

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
                    }
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
        return cuentas;


        } catch (Exception e) {
            return cuentas;
        }
        
    }

    public Cuenta firebaseGetCuentaById(String uid, String cuentaId){
        CountDownLatch latch = new CountDownLatch(1);
        final Cuenta[] cuenta = new Cuenta[1];

        try {
        // Referencia a la base de datos de Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        // Referencia al nodo de las cuentas del usuario
        DatabaseReference cuentaRef = databaseReference.child("users").child(uid).child("cuentas").child(cuentaId);
        // Escuchar los datos de Firebase
        cuentaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Cuenta cuentaLocal = dataSnapshot.getValue(Cuenta.class);
                String key = dataSnapshot.getKey();
                if(cuentaLocal != null){
                    cuentaLocal.setId(key);
                    cuenta[0] = cuentaLocal;
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


        return cuenta[0];
            
        } catch (Exception e) {
            return cuenta[0];
        }

    }


}

package com.finanzas.app_back.service;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.springframework.stereotype.Service;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Tarjetas.TarjetaDto;
import com.finanzas.app_back.dto.Tarjetas.TarjetaList;
import com.finanzas.app_back.model.Tarjeta;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@Service
public class TarjetasService {

    public GenericResponse registrarTarjeta(String uid ,TarjetaDto dto) {
        GenericResponse response = new GenericResponse();

        try {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference nuevaTarjetaRef = databaseReference.child("users").child(uid).child("tarjetas").push();
            String idGenerado = nuevaTarjetaRef.getKey();
            nuevaTarjetaRef.setValueAsync(dto);

            dto.setId(idGenerado);

            response.setCoderr("0000");
            response.setMessage("Tarjeta registrada exitosamente.");
            response.setData(dto);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al registrar la tarjeta: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse obtenerTarjetas(String uid) {
        GenericResponse response = new GenericResponse();
        ArrayList<Tarjeta> tarjetas = new ArrayList<>();
    
        try {

            tarjetas = firebaseGetTarjetas(uid);

            if (tarjetas.isEmpty()) {
                response.setCoderr("0001");
                response.setMessage("No se encontraron tarjetas.");
                return response;
            }
            
            for (Tarjeta tarjeta : tarjetas) {

                // suma valores
                
            }

            TarjetaList tarjetaList = new TarjetaList();
            tarjetaList.setTarjetas(tarjetas);


            response.setCoderr("0000");
            response.setMessage("Tarjetas obtenidas exitosamente.");

            response.setData(tarjetaList); // Asumiendo que 'tarjetas' es la lista obtenida
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al obtener las tarjetas: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse eliminarTarjeta(String uid, String tarjetaId) {
        GenericResponse response = new GenericResponse();

        try {
            
            Tarjeta tarjeta = firebaseGetTarjetaById(uid, tarjetaId);

            if(tarjeta == null){
                response.setCoderr("0001");
                response.setMessage("Tarjeta no encontrada.");
                return response;
            }

            if(tarjeta.isActiva()){
                response.setCoderr("0002");
                response.setMessage("No se puede eliminar una tarjeta activa.");
                return response;
            }   

            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Referencia al nodo de la tarjeta específica del usuario
            DatabaseReference tarjetaRef = databaseReference.child("users").child(uid).child("tarjetas").child(tarjetaId);

            // Eliminar la tarjeta
            tarjetaRef.removeValueAsync();

            response.setCoderr("0000");
            response.setMessage("Tarjeta eliminada exitosamente.");
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al eliminar la tarjeta: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse actualizarTarjeta(String uid, String tarjetaId, TarjetaDto updatedTarjetaDto) {
        GenericResponse response = new GenericResponse();

        try {

            Tarjeta tarjeta = firebaseGetTarjetaById(uid, tarjetaId);

            if(tarjeta == null){
                response.setCoderr("0001");
                response.setMessage("Tarjeta no encontrada.");
                return response;
            } 
            tarjeta.setNombre(updatedTarjetaDto.getNombre());
            tarjeta.setDescripcion(updatedTarjetaDto.getDescripcion());
            tarjeta.setInstitucion(updatedTarjetaDto.getInstitucion());
            tarjeta.setDpago(updatedTarjetaDto.getDpago());
            tarjeta.setDcorte(updatedTarjetaDto.getDcorte());

            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Referencia al nodo de la tarjeta específica del usuario
            DatabaseReference tarjetaRef = databaseReference.child("users").child(uid).child("tarjetas").child(tarjetaId);

            // Actualizar los datos de la tarjeta
            tarjetaRef.setValueAsync(tarjeta);

            response.setCoderr("0000");
            response.setMessage("Tarjeta actualizada exitosamente.");

            
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al actualizar la tarjeta: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse consultaTarjeta(String uid, String tarjetaId) {
        GenericResponse response = new GenericResponse();

        try {

            Tarjeta tarjeta = firebaseGetTarjetaById(uid, tarjetaId);

            if(tarjeta == null){
                response.setCoderr("0001");
                response.setMessage("Tarjeta no encontrada.");
                return response;
            }

            response.setCoderr("0000");
            response.setMessage("Tarjeta obtenida exitosamente.");
            response.setData(tarjeta);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al obtener la tarjeta: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse ordenTarjetas(String uid, ArrayList<TarjetaDto> tarjetas){

        GenericResponse response = new GenericResponse();

        CountDownLatch latch = new CountDownLatch(1);
        final Tarjeta[] tarjeta = new Tarjeta[1];
        final int[] orden = {1};

        try {
            // Referencia a la base de datos de Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        

        // Recorrer el ArrayList de tarjetas
        for (TarjetaDto tarjetaFor : tarjetas) {
            
            if (tarjetaFor.getId() != null) { // Asegurarse de que la tarjeta tenga un ID válido
                // Referencia al nodo de la tarjeta específica
                DatabaseReference tarjetaRef = databaseReference.child("users").child(uid).child("tarjetas").child(tarjetaFor.getId());

                // Escuchar los datos de Firebase
                tarjetaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        tarjeta[0] = dataSnapshot.getValue(Tarjeta.class);

                        System.out.println("Tarjeta obtenida: " + tarjeta[0].toString());

                        if (tarjeta[0] != null) {
                            tarjeta[0].setOrden(orden[0]);
                            orden[0]++;
                            // Actualizar los datos de la tarjeta en Firebase
                            tarjetaRef.setValueAsync(tarjeta[0]);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("Error al consultar la tarjeta: " + databaseError.getMessage());
                        latch.countDown(); // Liberar el latch cuando se complete la lectura
                    }
                });

                
            }
        }

        latch.countDown(); // Liberar el latch cuando se complete la lectura

        response.setCoderr("0000");
        response.setMessage("Tarjetas actualizadas exitosamente.");
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al obtener la tarjeta: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse activarTarjeta(String uid, String tarjetaId, boolean activa) {
        GenericResponse response = new GenericResponse();

        try {


            Tarjeta tarjeta = firebaseGetTarjetaById(uid, tarjetaId);

            if(tarjeta == null){
                response.setCoderr("0001");
                response.setMessage("Tarjeta no encontrada.");
                return response;
            }

            tarjeta.setActiva(activa);


            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            // Referencia al nodo de la tarjeta específica del usuario
            DatabaseReference tarjetaRef = databaseReference.child("users").child(uid).child("tarjetas").child(tarjetaId);
            // Actualizar los datos de la tarjeta
            tarjetaRef.setValueAsync(tarjeta);

            response.setCoderr("0000");
            response.setMessage("Tarjeta actualizada exitosamente.");
            response.setData(activa);

            
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al actualizar la tarjeta: " + e.getMessage());
        }

        return response;
    }



    public ArrayList<Tarjeta> firebaseGetTarjetas(String uid){
        ArrayList<Tarjeta> tarjetas = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        try {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference tarjetasRef = databaseReference.child("users").child(uid).child("tarjetas");

        tarjetasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot tarjetaSnapshot : dataSnapshot.getChildren()) {
                    Tarjeta tarjeta = tarjetaSnapshot.getValue(Tarjeta.class);
                    String key = tarjetaSnapshot.getKey();

                    if(tarjeta != null){
                        tarjeta.setId(key);
                        tarjetas.add(tarjeta);
                    }
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error al consultar las tarejtas: " + databaseError.getMessage());
                latch.countDown();
            }

        });

        latch.await(); // Esperar a que se complete la operación asíncrona
        return tarjetas;


        } catch (Exception e) {
            return tarjetas;
        }
        
    }

    public Tarjeta firebaseGetTarjetaById(String uid, String tarjetaId){
        CountDownLatch latch = new CountDownLatch(1);
        final Tarjeta[] tarjeta = new Tarjeta[1];

        try {
        // Referencia a la base de datos de Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        // Referencia al nodo de las tarjetas del usuario
        DatabaseReference tarjetaRef = databaseReference.child("users").child(uid).child("tarjetas").child(tarjetaId);
        // Escuchar los datos de Firebase
        tarjetaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Tarjeta tarjetaLocal = dataSnapshot.getValue(Tarjeta.class);
                String key = dataSnapshot.getKey();
                if(tarjetaLocal != null){
                    tarjetaLocal.setId(key);
                    tarjeta[0] = tarjetaLocal;
                }
                latch.countDown(); // Liberar el latch cuando se complete la lectura
            } 
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error al consultar las tarjetas: " + databaseError.getMessage());
                latch.countDown(); // Liberar el latch cuando se complete la lectura
            }
        }); 

        latch.await(); // Esperar a que se complete la operación asíncrona


        return tarjeta[0];
            
        } catch (Exception e) {
            return tarjeta[0];
        }

    }


}

package com.finanzas.app_back.service;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.springframework.stereotype.Service;
import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Categorias.CategoriaDto;
import com.finanzas.app_back.dto.Categorias.CategoriasList;
import com.finanzas.app_back.model.Categoria;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

@Service
public class CategoriasService {

    public GenericResponse registrarCategoria(String uid ,CategoriaDto dto) {
        GenericResponse response = new GenericResponse();

        try {

            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Crear una nueva entrada en la base de datos bajo el UID del usuario
            DatabaseReference nuevaCategoriaRef = databaseReference.child("users").child(uid).child("categorias").push();
            String idGenerado = nuevaCategoriaRef.getKey(); // Obtener el ID único generado
            nuevaCategoriaRef.setValueAsync(dto);

            dto.setId(idGenerado);

            response.setCoderr("0000");
            response.setMessage("Categoria registrada exitosamente.");
            response.setData(dto);
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al registrar la categoria: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse obtenerCategorias(String uid) {
        GenericResponse response = new GenericResponse();
        ArrayList<Categoria> categorias = new ArrayList<>();
        ArrayList<Categoria> categoriasIngresos = new ArrayList<>();
        ArrayList<Categoria> categoriasEgresos = new ArrayList<>();
    
        try {

            categorias = firebaseGetCategorias(uid);

            if (categorias.isEmpty()) {
                response.setCoderr("0001");
                response.setMessage("No se encontraron categorias.");
                return response;
            }
            
            for (Categoria categoria : categorias) {

                if(categoria.getTipo().equals("I")){
                    categoriasIngresos.add(categoria);
                }else if(categoria.getTipo().equals("E")){
                    categoriasEgresos.add(categoria);
                }
                
            }

            CategoriasList categoriasList = new CategoriasList();
            categoriasList.setCategoriasIngresos(categoriasIngresos);
            categoriasList.setCategoriasEgresos(categoriasEgresos);

            response.setCoderr("0000");
            response.setMessage("Categorias obtenidas exitosamente.");

            response.setData(categoriasList); // Asumiendo que 'categorias' es la lista obtenida
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al obtener las categorias: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse eliminarCategoria(String uid, String categoriaId) {
        GenericResponse response = new GenericResponse();

        try {
            
            Categoria categoria = firebaseGetCategoriaById(uid, categoriaId);

            if(categoria == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            }

            if(categoria.isActiva()){
                response.setCoderr("0002");
                response.setMessage("No se puede eliminar una categoria activa.");
                return response;
            }   

            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Referencia al nodo de la categoria específica del usuario
            DatabaseReference categoriaRef = databaseReference.child("users").child(uid).child("categorias").child(categoriaId);

            // Eliminar la categoria
            categoriaRef.removeValueAsync();

            response.setCoderr("0000");
            response.setMessage("Categoria eliminada exitosamente.");
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al eliminar la categoria: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse actualizarCategoria(String uid, String categoriaId, CategoriaDto updatedCategoriaDto) {
        GenericResponse response = new GenericResponse();

        try {

            Categoria categoria = firebaseGetCategoriaById(uid, categoriaId);

            if(categoria == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            } 
            categoria.setNombre(updatedCategoriaDto.getNombre());

            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            // Referencia al nodo de la categoria específica del usuario
            DatabaseReference categoriaRef = databaseReference.child("users").child(uid).child("categorias").child(categoriaId);

            // Actualizar los datos de la categoria
            categoriaRef.setValueAsync(categoria);

            response.setCoderr("0000");
            response.setMessage("Categoria actualizada exitosamente.");

            
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al actualizar la categoria: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse consultaCategoria(String uid, String categoriaId) {
        GenericResponse response = new GenericResponse();

        try {

            Categoria categoria = firebaseGetCategoriaById(uid, categoriaId);

            if(categoria == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            }

            response.setCoderr("0000");
            response.setMessage("Categoria obtenida exitosamente.");
            response.setData(categoria);

        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al obtener la categoria: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse ordenCategorias(String uid, ArrayList<CategoriaDto> categorias){

        GenericResponse response = new GenericResponse();

        CountDownLatch latch = new CountDownLatch(1);
        final Categoria[] categoria = new Categoria[1];
        final int[] orden = {1};

        try {
            // Referencia a la base de datos de Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        

        // Recorrer el ArrayList de categorias
        for (CategoriaDto categoriaFor : categorias) {
            
            if (categoriaFor.getId() != null) { // Asegurarse de que la categoria tenga un ID válido
                // Referencia al nodo de la categoria específica
                DatabaseReference categoriaRef = databaseReference.child("users").child(uid).child("categorias").child(categoriaFor.getId());

                // Escuchar los datos de Firebase
                categoriaRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        categoria[0] = dataSnapshot.getValue(Categoria.class);

                        System.out.println("Categoria obtenida: " + categoria[0].toString());

                        if (categoria[0] != null) {
                            categoria[0].setOrden(orden[0]);
                            orden[0]++;
                            // Actualizar los datos de la categoria en Firebase
                            categoriaRef.setValueAsync(categoria[0]);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("Error al consultar la categoria: " + databaseError.getMessage());
                        latch.countDown(); // Liberar el latch cuando se complete la lectura
                    }
                });

                
            }
        }

        latch.countDown(); // Liberar el latch cuando se complete la lectura

        response.setCoderr("0000");
        response.setMessage("Categorias actualizadas exitosamente.");
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al obtener la categoria: " + e.getMessage());
        }

        return response;
    }

    public GenericResponse activarCategoria(String uid, String categoriaId, boolean activa) {
        GenericResponse response = new GenericResponse();

        try {


            Categoria categoria = firebaseGetCategoriaById(uid, categoriaId);

            if(categoria == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            }

            categoria.setActiva(activa);


            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            // Referencia al nodo de la categoria específica del usuario
            DatabaseReference categoriaRef = databaseReference.child("users").child(uid).child("categorias").child(categoriaId);
            // Actualizar los datos de la categoria
            categoriaRef.setValueAsync(categoria);

            response.setCoderr("0000");
            response.setMessage("Categoria actualizada exitosamente.");
            response.setData(activa);

            
        } catch (Exception e) {
            response.setCoderr("9999");
            response.setMessage("Error al actualizar la categoria: " + e.getMessage());
        }

        return response;
    }



    public ArrayList<Categoria> firebaseGetCategorias(String uid){
        ArrayList<Categoria> categorias = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        try {
            // Referencia a la base de datos de Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        // Referencia al nodo de las categorias del usuario
        DatabaseReference categoriasRef = databaseReference.child("users").child(uid).child("categorias");


        // Escuchar los datos de Firebase
        categoriasRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot categoriaSnapshot : dataSnapshot.getChildren()) {
                    Categoria categoria = categoriaSnapshot.getValue(Categoria.class);
                    String key = categoriaSnapshot.getKey();

                    if(categoria != null){
                        categoria.setId(key);
                        categorias.add(categoria);
                    }
                }
                latch.countDown(); // Liberar el latch cuando se complete la lectura
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error al consultar las categorias: " + databaseError.getMessage());
                latch.countDown(); // Liberar el latch cuando se complete la lectura
            }

        });

        latch.await(); // Esperar a que se complete la operación asíncrona
        return categorias;


        } catch (Exception e) {
            return categorias;
        }
        
    }

    public Categoria firebaseGetCategoriaById(String uid, String categoriaId){
        CountDownLatch latch = new CountDownLatch(1);
        final Categoria[] categoria = new Categoria[1];

        try {
        // Referencia a la base de datos de Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        // Referencia al nodo de las categorias del usuario
        DatabaseReference categoriaRef = databaseReference.child("users").child(uid).child("categorias").child(categoriaId);
        // Escuchar los datos de Firebase
        categoriaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Categoria categoriaLocal = dataSnapshot.getValue(Categoria.class);
                String key = dataSnapshot.getKey();
                if(categoriaLocal != null){
                    categoriaLocal.setId(key);
                    categoria[0] = categoriaLocal;
                }
                latch.countDown(); // Liberar el latch cuando se complete la lectura
            } 
            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error al consultar las categorias: " + databaseError.getMessage());
                latch.countDown(); // Liberar el latch cuando se complete la lectura
            }
        }); 

        latch.await(); // Esperar a que se complete la operación asíncrona


        return categoria[0];
            
        } catch (Exception e) {
            return categoria[0];
        }

    }


}

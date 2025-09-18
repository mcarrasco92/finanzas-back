package com.finanzas.app_back.service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
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

    public GenericResponse registrarCategoria(String uid, CategoriaDto dto) {
        GenericResponse response = new GenericResponse();

        try {
            DatabaseReference nuevaCategoriaRef = getCategoriasReference(uid).push();
            String idGenerado = nuevaCategoriaRef.getKey();
            dto.setId(idGenerado);
            nuevaCategoriaRef.setValueAsync(dto);

            response.setCoderr("0000");
            response.setMessage("Categoría registrada exitosamente.");
            response.setData(dto);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al registrar la categoría");
        }

        return response;
    }

    public GenericResponse obtenerCategorias(String uid) {
        GenericResponse response = new GenericResponse();

        try {
            ArrayList<Categoria> categorias = firebaseGetCategorias(uid).join();

            if (categorias.isEmpty()) {
                response.setCoderr("0001");
                response.setMessage("No se encontraron categorías.");
                return response;
            }

            CategoriasList categoriasList = separarCategoriasPorTipo(categorias);

            response.setCoderr("0000");
            response.setMessage("Categorías obtenidas exitosamente.");
            response.setData(categoriasList);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al obtener las categorías");
        }

        return response;
    }

    public GenericResponse eliminarCategoria(String uid, String categoriaId) {
        GenericResponse response = new GenericResponse();

        try {
            Categoria categoria = validarCategoriaExistente(uid, categoriaId);

            if (categoria.isActiva()) {
                response.setCoderr("0002");
                response.setMessage("No se puede eliminar una categoría activa.");
                return response;
            }

            getCategoriaReference(uid, categoriaId).removeValueAsync();

            response.setCoderr("0000");
            response.setMessage("Categoría eliminada exitosamente.");
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al eliminar la categoría");
        }

        return response;
    }

    public GenericResponse actualizarCategoria(String uid, String categoriaId, CategoriaDto updatedCategoriaDto) {
        GenericResponse response = new GenericResponse();

        try {
            Categoria categoria = validarCategoriaExistente(uid, categoriaId);

            categoria.setNombre(updatedCategoriaDto.getNombre());
            categoria.setTipo(updatedCategoriaDto.getTipo());

            getCategoriaReference(uid, categoriaId).setValueAsync(categoria);

            response.setCoderr("0000");
            response.setMessage("Categoría actualizada exitosamente.");
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al actualizar la categoría");
        }

        return response;
    }

    public GenericResponse consultaCategoria(String uid, String categoriaId) {
        GenericResponse response = new GenericResponse();

        try {

            Categoria categoria = firebaseGetCategoriaById(uid, categoriaId).join();

            if(categoria == null){
                response.setCoderr("0001");
                response.setMessage("Categoria no encontrada.");
                return response;
            }

            response.setCoderr("0000");
            response.setMessage("Categoria obtenida exitosamente.");
            response.setData(categoria);
            return response;

        } catch (Exception e) {
            return manejarExcepcion(e, "Error al obtener la categoría");
        }

        
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

        return response;

        } catch (Exception e) {
            return manejarExcepcion(e, "Error al actualizar las categorias");
        }
        
    }

    public GenericResponse activarCategoria(String uid, String categoriaId, boolean activa) {
        GenericResponse response = new GenericResponse();

        try {
            Categoria categoria = validarCategoriaExistente(uid, categoriaId);

            categoria.setActiva(activa);
            getCategoriaReference(uid, categoriaId).setValueAsync(categoria);

            response.setCoderr("0000");
            response.setMessage("Categoría " + (activa ? "activada" : "desactivada") + " exitosamente.");
            response.setData(activa);
        } catch (Exception e) {
            return manejarExcepcion(e, "Error al activar/desactivar la categoría");
        }

        return response;
    }



    public CompletableFuture<ArrayList<Categoria>> firebaseGetCategorias(String uid) {
        CompletableFuture<ArrayList<Categoria>> future = new CompletableFuture<>();
        ArrayList<Categoria> categorias = new ArrayList<>();
    
        try {
            // Referencia a la base de datos de Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
            DatabaseReference categoriasRef = databaseReference.child("users").child(uid).child("categorias");
    
            // Escuchar los datos de Firebase
            categoriasRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot categoriaSnapshot : dataSnapshot.getChildren()) {
                        Categoria categoria = categoriaSnapshot.getValue(Categoria.class);
                        if (categoria != null) {
                            categoria.setId(categoriaSnapshot.getKey());
                            categorias.add(categoria);
                        }
                    }
                    future.complete(categorias); // Completar el CompletableFuture con la lista de categorías
                }
    
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("Error al consultar las categorías: " + databaseError.getMessage());
                    future.completeExceptionally(new RuntimeException("Error al consultar las categorías: " + databaseError.getMessage()));
                }
            });
        } catch (Exception e) {
            future.completeExceptionally(e); // Completar con una excepción si ocurre un error
        }
    
        return future;
    }

    public CompletableFuture<Categoria> firebaseGetCategoriaById(String uid, String categoriaId) {
    CompletableFuture<Categoria> future = new CompletableFuture<>();

    try {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference categoriaRef = databaseReference.child("users").child(uid).child("categorias").child(categoriaId);

        categoriaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Categoria categoria = dataSnapshot.getValue(Categoria.class);
                if (categoria != null) {
                    categoria.setId(dataSnapshot.getKey());
                    future.complete(categoria); // Completar el CompletableFuture con el resultado
                } else {
                    future.complete(null); // Completar con null si no se encuentra la categoría
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error al consultar la categoría: " + databaseError.getMessage());
                future.completeExceptionally(new RuntimeException("Error al consultar la categoría: " + databaseError.getMessage()));
            }
        });
    } catch (Exception e) {
        future.completeExceptionally(e); // Completar con una excepción si ocurre un error
    }

    return future;
}

    private CategoriasList separarCategoriasPorTipo(ArrayList<Categoria> categorias) {
        ArrayList<Categoria> categoriasIngresos = new ArrayList<>();
        ArrayList<Categoria> categoriasEgresos = new ArrayList<>();

        for (Categoria categoria : categorias) {
            if ("I".equals(categoria.getTipo())) {
                categoriasIngresos.add(categoria);
            } else if ("E".equals(categoria.getTipo())) {
                categoriasEgresos.add(categoria);
            }
        }

        CategoriasList categoriasList = new CategoriasList();
        categoriasList.setCategoriasIngresos(categoriasIngresos);
        categoriasList.setCategoriasEgresos(categoriasEgresos);

        return categoriasList;
    }

    private DatabaseReference getCategoriasReference(String uid) {
        return FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("categorias");
    }

    private DatabaseReference getCategoriaReference(String uid, String categoriaId) {
        return getCategoriasReference(uid).child(categoriaId);
    }

    private GenericResponse manejarExcepcion(Exception e, String mensaje) {
        GenericResponse response = new GenericResponse();
        response.setCoderr("9999");
        response.setMessage(mensaje + ": " + e.getMessage());
        return response;
    }

    private Categoria validarCategoriaExistente(String uid, String categoriaId) {
        Categoria categoria = firebaseGetCategoriaById(uid, categoriaId).join();
        if (categoria == null) {
            throw new RuntimeException("Categoría no encontrada.");
        }
        return categoria;
    }


}

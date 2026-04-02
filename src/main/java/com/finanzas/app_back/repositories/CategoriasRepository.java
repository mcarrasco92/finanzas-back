package com.finanzas.app_back.repositories;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.finanzas.app_back.dto.Categorias.CategoriaDto;
import com.finanzas.app_back.model.Categoria;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;

@Repository
public class CategoriasRepository {
        @Autowired
    private Firestore firestore;

    private static final String COLLECTION_NAME = "categorias";

    public String newCategoria(String spaceId, Categoria categoria) throws ExecutionException, InterruptedException {

        System.out.println("Categoria a registrar: " + categoria);

        CollectionReference categorias = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME);
        DocumentReference document = categorias.document();

        ApiFuture<WriteResult> writeResult = document.set(categoria);
        writeResult.get();

        return document.getId();
    }

    public ArrayList<CategoriaDto> getCategorias(String spaceId) throws ExecutionException, InterruptedException {
        CollectionReference categoriasRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> querySnapshot = categoriasRef.get();

        ArrayList<CategoriaDto> categoriasList = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
            CategoriaDto categoria = document.toObject(CategoriaDto.class);
            categoria.setId(document.getId());
            categoriasList.add(categoria);
        }

        return categoriasList;
    }

    public CategoriaDto getCategoriaById(String spaceId, String categoriaId) throws ExecutionException, InterruptedException {
        DocumentReference categoriaRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(categoriaId);
        ApiFuture<DocumentSnapshot> future = categoriaRef.get();
        DocumentSnapshot document = future.get();

        if (document.exists()) {
            CategoriaDto categoria = document.toObject(CategoriaDto.class);
            categoria.setId(document.getId());
            return categoria;
        } else {
            return null;
        }
    }

    public void updateCategoria(String spaceId, String categoriaId, Categoria categoria) throws ExecutionException, InterruptedException {
        DocumentReference categoriaRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(categoriaId);
        ApiFuture<WriteResult> writeResult = categoriaRef.set(categoria);
        writeResult.get();
    }

    public void deleteCategoria(String spaceId, String categoriaId) throws ExecutionException, InterruptedException {
        DocumentReference categoriaRef = firestore.collection("spaces").document(spaceId).collection(COLLECTION_NAME).document(categoriaId);
        ApiFuture<WriteResult> writeResult = categoriaRef.delete();
        writeResult.get();
    }

    public Boolean getExistTransaccionesByCat(String spaceId, String categoriaId) throws ExecutionException, InterruptedException {
        CollectionReference transaccionesRef = firestore.collection("spaces").document(spaceId).collection("transacciones");

        ApiFuture<QuerySnapshot> queryIngreso = transaccionesRef
            .whereEqualTo("catIngresoId", categoriaId)
            .get();

        ApiFuture<QuerySnapshot> queryEgreso = transaccionesRef
            .whereEqualTo("catEgresoId", categoriaId)
            .get();

        boolean existeEnIngreso = !queryIngreso.get().isEmpty();
        boolean existeEnEgreso = !queryEgreso.get().isEmpty();

        return existeEnIngreso || existeEnEgreso;
    }


}

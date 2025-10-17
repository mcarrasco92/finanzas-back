package com.finanzas.app_back.config;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

@Configuration
public class FirestoreConfig {

    @Value("${firebase.credentials.path}")   
    private String firebaseCredentialsPath;

    @Bean
    public Firestore firestore() throws IOException {
        // Ruta al archivo serviceAccountKey.json
        FileInputStream serviceAccount = new FileInputStream(firebaseCredentialsPath);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        // Inicializar Firebase
        if (FirebaseApp.getApps().isEmpty()) {
            System.out.println("Inicializando Firebase con credenciales de: " + firebaseCredentialsPath);
            FirebaseApp.initializeApp(options);
        }

        // Retornar la instancia de Firestore
        return FirestoreClient.getFirestore();
    }

    public void closeFirestore(Firestore firestore) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Cerrando conexión con Firestore...");
                firestore.close();
            } catch (Exception e) {
                System.err.println("Error al cerrar Firestore: " + e.getMessage());
            }
        }));
    }
}

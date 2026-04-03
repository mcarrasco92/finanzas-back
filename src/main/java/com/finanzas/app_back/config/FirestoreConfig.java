package com.finanzas.app_back.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.annotation.PreDestroy;

@Configuration
public class FirestoreConfig {

    @Value("${firebase.credentials.path}")
    private String firebaseCredentialsPath;

    private Firestore firestoreInstance;

    @Bean
    public Firestore firestore() throws IOException {
        InputStream serviceAccount = new ClassPathResource(firebaseCredentialsPath).getInputStream();

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            System.out.println("Inicializando Firebase con credenciales de: " + firebaseCredentialsPath);
            FirebaseApp.initializeApp(options);
        }

        firestoreInstance = FirestoreClient.getFirestore();
        return firestoreInstance;
    }

    @PreDestroy
    public void closeFirestore() {
        try {
            if (firestoreInstance != null) {
                System.out.println("Cerrando conexión con Firestore...");
                firestoreInstance.close();
            }
            FirebaseApp.getApps().forEach(FirebaseApp::delete);
        } catch (Exception e) {
            System.err.println("Error al cerrar Firestore: " + e.getMessage());
        }
    }
}

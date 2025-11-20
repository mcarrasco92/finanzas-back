package com.finanzas.app_back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class TransaccionRecurrenteJob {
    @Autowired
    private com.finanzas.app_back.repositories.TransaccionRecurrenteRepository transaccionRecurrenteRepository;
    @Autowired
    private com.finanzas.app_back.service.TransaccionesService transaccionesService;

    // Ejecuta todos los días a las 09:00 am
    @Scheduled(cron = "0 46 17 * * *")
    public void ejecutarJobTransaccionesRecurrentes() {
        try {
            transaccionRecurrenteRepository.generaTransaccionesRecurrentes(transaccionesService);
            System.out.println("Job de transacciones recurrentes ejecutado correctamente.");
        } catch (Exception e) {
            System.err.println("Error al ejecutar el job de transacciones recurrentes: " + e.getMessage());
        }
    }
}
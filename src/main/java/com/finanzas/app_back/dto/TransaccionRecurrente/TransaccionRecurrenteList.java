package com.finanzas.app_back.dto.TransaccionRecurrente;
import java.util.ArrayList;
import lombok.Data;

@Data
public class TransaccionRecurrenteList {
    private ArrayList<TransaccionRecurrenteDto> transaccionesRecurrentes;
}

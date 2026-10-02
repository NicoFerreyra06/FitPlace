package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EjercicioRutinaRequest {
    @NotNull(message = "Debe indicar el ejercicio")
    private Long ejercicioId;
    @Min(1)
    private int series;
    @Min(1)
    private int repeticiones;
}

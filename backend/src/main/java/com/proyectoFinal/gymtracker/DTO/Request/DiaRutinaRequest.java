package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.DayOfWeek;
import java.util.List;

@Data
public class DiaRutinaRequest {
    @NotNull(message = "Debe indicar el dia de la semana")
    private DayOfWeek diaDeLaSemana;
    @Valid
    @NotEmpty(message = "Debe indicar el ejercicio")
    private List<EjercicioRutinaRequest> ejercicios;
}

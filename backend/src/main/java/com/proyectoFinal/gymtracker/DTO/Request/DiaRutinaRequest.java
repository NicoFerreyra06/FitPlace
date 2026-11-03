package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.DayOfWeek;
import java.util.List;

@Data
public class DiaRutinaRequest {
    @NotNull(message = "Debe indicar el dia de la semana")
    private DayOfWeek diaDeLaSemana;
    @NotBlank(message = "Cada día de la rutina necesita un nombre")
    @Size(max = 40, message = "El nombre del día no puede superar los 40 caracteres")
    private String nombre;
    @Valid
    @NotEmpty(message = "Debe indicar el ejercicio")
    private List<EjercicioRutinaRequest> ejercicios;
}

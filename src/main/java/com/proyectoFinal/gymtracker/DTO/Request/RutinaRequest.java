package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.util.List;

@Data
public class RutinaRequest {

    @NotBlank(message = "El nombre de la rutina es obligatorio")
    private String nombre;
    @PositiveOrZero
    private Double precio;

    private List<DiaRutinaRequest> dias;
}

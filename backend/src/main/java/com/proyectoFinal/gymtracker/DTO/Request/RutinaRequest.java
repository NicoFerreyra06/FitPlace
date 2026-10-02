package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RutinaRequest {

    @NotBlank(message = "El nombre de la rutina es obligatorio")
    private String nombre;
    private Boolean esPublica;

    private List<DiaRutinaRequest> dias;
}

package com.proyectoFinal.gymtracker.DTO.Response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MarcaEjercicioResponse {
    private Long id;
    private String nombreEjercicio;
    private Double pesoLevantado;
    private Integer repeticionesLogradas;
}

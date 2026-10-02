package com.proyectoFinal.gymtracker.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EjercicioRutinaResponse {
    private Long id;
    private Long ejercicioId;
    private String nombreEjercicio;
    private Integer series;
    private Integer repeticiones;
}

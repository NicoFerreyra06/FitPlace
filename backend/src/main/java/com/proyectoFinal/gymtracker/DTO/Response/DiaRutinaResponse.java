package com.proyectoFinal.gymtracker.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiaRutinaResponse {
    private Long id;
    private DayOfWeek diaDeLaSemana;
    private List<EjercicioRutinaResponse> ejercicioRutinas;
}

package com.proyectoFinal.gymtracker.DTO.Response;

import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import lombok.Builder;

@Builder
public class MusculoResponse {
    private Long id;
    private String nombre;
    private GrupoMuscular grupoMuscular;
}

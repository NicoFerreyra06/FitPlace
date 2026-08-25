package com.proyectoFinal.gymtracker.DTO.Response;

import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class    MusculoResponse {
    private Long id;
    private String nombre;
    private GrupoMuscular grupoMuscular;
}

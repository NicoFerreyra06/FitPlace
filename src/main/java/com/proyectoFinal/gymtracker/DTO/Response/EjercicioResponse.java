package com.proyectoFinal.gymtracker.DTO.Response;

import com.proyectoFinal.gymtracker.Modelo.Musculo;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class EjercicioResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private List<Musculo> musculosPrincipales;
    private List<Musculo> musculosSecundarios;
}

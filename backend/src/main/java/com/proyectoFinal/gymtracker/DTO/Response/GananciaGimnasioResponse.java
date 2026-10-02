package com.proyectoFinal.gymtracker.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GananciaGimnasioResponse {
    private String nombreGimnasio;
    private Double totalIngresosBrutos;
    private Double comisionFitPlace;
    private Double gananciaNetaGimnasio;
}

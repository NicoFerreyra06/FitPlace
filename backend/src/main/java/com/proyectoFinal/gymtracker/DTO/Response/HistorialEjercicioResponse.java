package com.proyectoFinal.gymtracker.DTO.Response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistorialEjercicioResponse {

    private LocalDate fecha;
    private Double pesoMaximo;
}

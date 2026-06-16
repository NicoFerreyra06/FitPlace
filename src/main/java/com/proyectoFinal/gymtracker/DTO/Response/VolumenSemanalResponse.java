package com.proyectoFinal.gymtracker.DTO.Response;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VolumenSemanalResponse {
    private LocalDate inicioSemana;
    private Double volumenTotal;
    private Integer totalSeries;
}
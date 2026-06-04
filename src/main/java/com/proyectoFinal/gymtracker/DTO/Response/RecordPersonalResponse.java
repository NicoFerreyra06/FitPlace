package com.proyectoFinal.gymtracker.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecordPersonalResponse {
    private Long id;
    private Long ejercicioId;
    private String nombreUsuario;
    private String nombreEjercicio;
    private Double pesoMaximo;
    private LocalDate fechaLogro;

}

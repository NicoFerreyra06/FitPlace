package com.proyectoFinal.gymtracker.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntrenamientoLogResponse {

    private Long id;
    private String nombreUsuario;
    private String nombreRutina;
    private LocalDate fecha;
    private List<MarcaEjercicioResponse> marcasEjercicio;
}

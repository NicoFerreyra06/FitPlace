package com.proyectoFinal.gymtracker.DTO.Response;

import com.proyectoFinal.gymtracker.Enum.EstadoSuscripcion;
import com.proyectoFinal.gymtracker.Enum.MetodoPago;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
public class SuscripcionGimnasioResponse {
    private Long id;
    private Long idGimnasio;
    private Long idUsuario;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private MetodoPago metodoPago;
    private EstadoSuscripcion estadoSuscripcion;
}

package com.proyectoFinal.gymtracker.DTO.Response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String nombreGimnasio;
    private Long idUsuario;
    private String nombreUsuario;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private MetodoPago metodoPago;
    private EstadoSuscripcion estadoSuscripcion;
    private Double costo;
    @JsonProperty("isMercadoPagoVinculado")
    private Boolean isMercadoPagoVinculado;
}

package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EjercicioRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;
    @NotEmpty(message = "Debe seleccionar al menos un músculo principal")
    private List<Long> musculoPrincipalId;
    private List<Long> musculoSecundarioId;
}

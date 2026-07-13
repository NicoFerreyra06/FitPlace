package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EjercicioRequest {
    @NotBlank
    private String nombre;
    @Size(max = 100, message = "La descripción no puede superar los 100 caracteres")
    private String descripcion;
    @NotEmpty
    private List<Long> musculoPrincipalId;
    private List<Long> musculoSecundarioId;
}

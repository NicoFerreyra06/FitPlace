package com.proyectoFinal.gymtracker.DTO.Request;

import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MusculoRequest {

    @NotBlank(message = "El nombre del músculo no puede estar vacío")
    private String nombre;

    @NotNull(message = "Debe especificar a qué grupo muscular pertenece")
    private GrupoMuscular grupoMuscular;
}

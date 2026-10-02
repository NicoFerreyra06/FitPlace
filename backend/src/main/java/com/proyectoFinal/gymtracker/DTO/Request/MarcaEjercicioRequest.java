package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarcaEjercicioRequest {
    @NotNull @DecimalMin(value = "0.0")
    private Double pesoLevantado;
    @NotNull(message = "Debe indicar las repeticiones logradas")
    @Min(1)
    private Integer repeticionesLogradas;
    @NotNull(message = "Debe indicar el ejercicio")
    private Long ejercicioRutinaId;
}

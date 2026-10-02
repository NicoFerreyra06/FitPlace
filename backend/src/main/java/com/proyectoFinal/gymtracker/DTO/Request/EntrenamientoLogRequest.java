package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntrenamientoLogRequest {

    @NotNull(message = "Debe indicar la rutina")
    private Long idRutina;

    @NotEmpty(message = "Debe registrar al menos una marca")
    @Valid
    private List<MarcaEjercicioRequest> marcasEjercicio;
}

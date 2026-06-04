package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GimnasioRequest {

    @NotBlank(message = "El nombre del gimnasio es obligatorio.")
    @Size(min = 3, max = 60, message = "El nombre debe estar entre 3 y 60 caracteres")
    private String nombre;

    @NotBlank(message = "La direccion del gimnasio es obligatorio.")
    @Size(min = 3, max = 60, message = "La direccion debe estar entre 3 y 60 caracteres")
    private String direccion;

    @DecimalMin(value = "0.1", message = "El precio de la cuota debe ser mayor a 0.1")
    private Double precioCuota;

}

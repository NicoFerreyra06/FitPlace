package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
public class GimnasioRequest {

    @NotBlank(message = "El nombre del gimnasio es obligatorio.")
    @Size(min = 3, max = 60, message = "El nombre debe estar entre 3 y 60 caracteres")
    private String nombre;

    @NotBlank(message = "La direccion del gimnasio es obligatorio.")
    @Size(min = 3, max = 60, message = "La direccion debe estar entre 3 y 60 caracteres")
    private String direccion;

    @NotNull(message = "El horario de apertura es obligatorio")
    private LocalTime horarioApertura;
    @NotNull(message = "El horario de cierre es obligatorio")
    private LocalTime horarioCierre;

    @NotEmpty
    @ElementCollection(targetClass = DayOfWeek.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "gimnasio_dias", joinColumns = @JoinColumn(name = "gimnasio_id"))
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> diasAbierto;

    @DecimalMin(value = "0.1", message = "El precio de la cuota debe ser mayor a 0.1")
    private Double precioCuota;

    @NotNull(message = "El ID del administrador del gimnasio es obligatorio.")
    private Long adminId;

    @AssertTrue(message = "El horario de cierre debe ser posterior al horario de apertura")
    private boolean esValidoHorario(){
        if (horarioApertura == null || horarioCierre == null){
            return true;
        }
        return horarioCierre.isAfter(horarioApertura);
    }

}

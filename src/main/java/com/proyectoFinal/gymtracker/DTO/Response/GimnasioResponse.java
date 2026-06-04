package com.proyectoFinal.gymtracker.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GimnasioResponse {

    private Long id;
    private String nombre;
    private String direccion;
    private Double precioCuota;
    private Long idAdmin;
    private String nombreAdmin;
}

package com.proyectoFinal.gymtracker.DTO.Response;

import com.proyectoFinal.gymtracker.Enum.Rol;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AmigoResponse {
    private Long id;
    private String username;
    private Rol rol;
    private Integer rachaActualDias;
}
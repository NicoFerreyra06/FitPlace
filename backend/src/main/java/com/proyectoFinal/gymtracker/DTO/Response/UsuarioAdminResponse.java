package com.proyectoFinal.gymtracker.DTO.Response;

import com.proyectoFinal.gymtracker.Enum.Rol;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UsuarioAdminResponse {
    Long id;
    String username;
    String email;
    Rol rol;
}

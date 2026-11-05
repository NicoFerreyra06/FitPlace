package com.proyectoFinal.gymtracker.DTO.Response;

import com.proyectoFinal.gymtracker.Enum.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Perfil que ve un usuario cuando entra al perfil de un amigo.
 * A diferencia de UsuarioResponse, no incluye email, código de amigo ni rutina activa:
 * solo lo que el usuario comparte con su grupo de amigos.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PerfilAmigoResponse {
    private Long id;
    private String username;
    private Rol rol;
    private Double peso;
    private Double altura;
    private Integer rachaActualDias;
    private Integer rachaMaximaDias;
}

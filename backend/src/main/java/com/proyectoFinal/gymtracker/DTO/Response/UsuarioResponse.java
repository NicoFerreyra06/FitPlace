package com.proyectoFinal.gymtracker.DTO.Response;

import com.proyectoFinal.gymtracker.Enum.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioResponse {
    private Long id;
    private String username;
    private String email;
    private Rol rol;
    private String codigoAmigo;
    private Double peso;
    private Double altura;
    private Double imc;
    private String categoriaImc;
    private Integer rachaActualDias;
    private Integer rachaMaximaDias;
}

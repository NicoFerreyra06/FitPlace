package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UsuarioRequest {
    @NotBlank(message = "Debe indicar su username")
    private String username;
    @Email
    @NotBlank(message = "Debe indicar su email")
    private String email;
    @NotBlank
    @Size(min = 3, max = 30, message = "La contraseña debe tener entre 3 y 30 caracteres")
    private String password;

    @NotNull(message = "El peso es obligatorio")
    @DecimalMin(value = "20.0", message = "El peso debe ser mayor a 20 kg")
    @DecimalMax(value = "500.0", message = "El peso ingresado no es válido")
    private Double peso;

    @NotNull
    @DecimalMin(value = "0.5", message = "La altura debe ser al menos 0.5 metros")
    @DecimalMax(value = "3.0", message = "La altura ingresada no es válida")
    private Double altura;
}

package com.proyectoFinal.gymtracker.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @Email
    @NotBlank(message = "Debe proporcionar un email")
    private String email;
    @NotBlank(message = "La contrasenia no puede estar vacía")
    private String password;
}

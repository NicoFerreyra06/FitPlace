package com.proyectoFinal.gymtracker.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ErrorResponse {

    private String message;
    private String timestamp;
}

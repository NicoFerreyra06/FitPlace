package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Services.MercadoPagoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/mercadopago")
@RequiredArgsConstructor
public class MercadoPagoAuthController {

    private final MercadoPagoAuthService mercadoPagoAuthService;

    @GetMapping("/vincular/{idGimnasio}")
    @PreAuthorize("hasRole('ADMIN_GIMNASIO') or hasRole('ADMIN')")
    public ResponseEntity<String> getVinculacionUrl(@PathVariable Long idGimnasio, @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(mercadoPagoAuthService.getVinculacion(idGimnasio, usuario));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> mercadopagoCallback(@RequestParam(value = "code", required = false) String code,
                                                    @RequestParam(value = "state", required = false) String state) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(mercadoPagoAuthService.mercadoPagoCallback(code, state)))
                .build();
    }
}

package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.Modelo.SuscripcionGimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Interfaces.IPagoService;
import com.proyectoFinal.gymtracker.Services.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final SuscripcionService suscripcionService;
    private final IPagoService IpagoService;

    @PostMapping("/{idSuscripcion}")
    public ResponseEntity<String> generarPago(@PathVariable Long idSuscripcion,
                                              @AuthenticationPrincipal Usuario usuario) {

        SuscripcionGimnasio suscripcion =
                suscripcionService.getByIdAndUser(idSuscripcion, usuario);

        String url = IpagoService.generarLinkPago(suscripcion);

        return ResponseEntity.ok(url);
    }
}

package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.Modelo.SuscripcionGimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Services.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suscripciones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    @PostMapping("/{idGimnasio}")
    public ResponseEntity<SuscripcionGimnasio> createSuscripcion(@AuthenticationPrincipal Usuario usuario,
                                                                 @PathVariable Long idGimnasio) {
        return new ResponseEntity<>(suscripcionService.createSuscripcion(idGimnasio, usuario), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN_GIMNASIO') or hasRole('ADMIN')")
    @PutMapping("/{idSuscripcion}/activar")
    public ResponseEntity<SuscripcionGimnasio> activarSuscripcion(@AuthenticationPrincipal Usuario usuario,
                                                                 @PathVariable Long idSuscripcion) {
        return new ResponseEntity<>(suscripcionService.activarSuscripcion(idSuscripcion, usuario), HttpStatus.OK);
    }

    @GetMapping("/mia")
    public ResponseEntity<SuscripcionGimnasio> getSuscripcion(@AuthenticationPrincipal Usuario usuario) {
        return new ResponseEntity<>(suscripcionService.getSuscripcionGimnasio(usuario), HttpStatus.OK);
    }

    @PutMapping("/{idSuscripcion}/cancelar")
    public ResponseEntity<SuscripcionGimnasio> cancelarSuscripcion(@AuthenticationPrincipal Usuario usuario,
                                                                    @PathVariable Long idSuscripcion) {

        return ResponseEntity.ok(
                suscripcionService.cancelarSuscripcion(idSuscripcion, usuario)
        );
    }
}

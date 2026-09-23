package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.DTO.Response.AmigoResponse;
import com.proyectoFinal.gymtracker.DTO.Response.UsuarioResponse;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Services.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios/me/amigos")
public class AmistadController {

    private final SocialService socialService;

    @PostMapping("/{codigoAmigo}")
    public ResponseEntity<UsuarioResponse> agregarAmigo(@AuthenticationPrincipal Usuario usuario,
                                                        @PathVariable String codigoAmigo) {
        return ResponseEntity.ok(socialService.agregarAmigo(usuario, codigoAmigo));
    }

    @DeleteMapping("/{amigoId}")
    public ResponseEntity<UsuarioResponse> eliminarAmigo(@PathVariable Long amigoId,
                                                         @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(socialService.eliminarAmigo(amigoId, usuario));
    }

    @GetMapping
    public ResponseEntity<List<AmigoResponse>> getAmigos(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(socialService.getAmigos(usuario));
    }

    @GetMapping("/{amigoId}/perfil")
    public ResponseEntity<UsuarioResponse> getPerfilAmigo(@AuthenticationPrincipal Usuario usuario,
                                                          @PathVariable Long amigoId) {
        return ResponseEntity.ok(socialService.verPerfilAmigo(usuario, amigoId));
    }
}

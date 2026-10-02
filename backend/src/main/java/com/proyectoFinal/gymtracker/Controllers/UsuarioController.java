package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.DTO.Request.LoginRequest;
import com.proyectoFinal.gymtracker.DTO.Request.PerfilUpdateRequest;
import com.proyectoFinal.gymtracker.DTO.Request.UsuarioRequest;
import com.proyectoFinal.gymtracker.DTO.Response.AmigoResponse;
import com.proyectoFinal.gymtracker.DTO.Response.GananciaGimnasioProjection;
import com.proyectoFinal.gymtracker.DTO.Response.LoginResponse;
import com.proyectoFinal.gymtracker.DTO.Response.UsuarioResponse;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Services.AuthService;
import com.proyectoFinal.gymtracker.Services.SocialService;
import com.proyectoFinal.gymtracker.Services.TutoriaService;
import com.proyectoFinal.gymtracker.Services.UsuarioService;
import com.proyectoFinal.gymtracker.Enum.Rol;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> verPerfilPropio(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.verPerfilPropio(usuario));
    }

    @GetMapping("/{idUsuario}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> verPerfilOtroUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(usuarioService.verPerfilOtroUsuario(idUsuario));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> editarPerfil(@AuthenticationPrincipal Usuario usuario,
                                                        @Valid @RequestBody PerfilUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.editarPerfil(usuario, request));
    }

    @PutMapping("/me/rutina-activa/{idRutina}")
    public ResponseEntity<UsuarioResponse> activarRutina(@AuthenticationPrincipal Usuario usuario,
                                                         @PathVariable Long idRutina) {
        return ResponseEntity.ok(usuarioService.activarRutina(usuario, idRutina));
    }

    @GetMapping("/entrenadores")
    public ResponseEntity<List<UsuarioResponse>> getEntrenadores() {
        return ResponseEntity.ok(usuarioService.getEntrenadores());
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> getUsuarios(@PageableDefault (size = 10) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.getUsuarios(pageable));
    }

    @PutMapping("/{idUsuario}/rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> cambiarRol(@PathVariable Long idUsuario, @RequestParam Rol nuevoRol) {
        return ResponseEntity.ok(usuarioService.cambiarRol(idUsuario, nuevoRol));
    }

    @GetMapping("/ganancias")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GananciaGimnasioProjection>> obtenerGananciasPorGimnasio(
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta
    ) {
        return ResponseEntity.ok(usuarioService.obtenerGananciasPorGimnasio(desde, hasta));
    }
}
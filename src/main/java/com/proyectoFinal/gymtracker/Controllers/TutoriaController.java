package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.DTO.Response.UsuarioResponse;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Services.TutoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class TutoriaController {

    private final TutoriaService tutoriaService;

    @PutMapping("/me/entrenador/{idEntrenador}")
    public ResponseEntity<UsuarioResponse> asignarEntrenador(@PathVariable Long idEntrenador,
                                                             @AuthenticationPrincipal Usuario usuario ) {
        return ResponseEntity.ok(tutoriaService.asignarEntrenador(idEntrenador, usuario));
    }

    @DeleteMapping("/me/entrenador")
    public ResponseEntity<UsuarioResponse> eliminarEntrenador(@AuthenticationPrincipal Usuario usuario ) {
        return ResponseEntity.ok(tutoriaService.eliminarEntrenador(usuario.getId()));
    }

    @GetMapping("/me/alumnos")
    public ResponseEntity<List<UsuarioResponse>> getAlumnos(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(tutoriaService.getAlumnos(usuario.getId()));
    }

    @GetMapping("/me/entrenador")
    public ResponseEntity<UsuarioResponse> getEntrenadorActual(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(tutoriaService.verEntrenadorActual(usuario.getId()));
    }

    @PreAuthorize("hasRole('ENTRENADOR')")
    @PutMapping("/{idAlumno}/rutina-activa/{idRutina}")
    public ResponseEntity<UsuarioResponse> asignarRutinaAAlumno(
            @AuthenticationPrincipal Usuario entrenador,
            @PathVariable Long idAlumno,
            @PathVariable Long idRutina) {
        return ResponseEntity.ok(tutoriaService.asignarRutinaAAlumno(entrenador, idAlumno, idRutina));
    }

    @PreAuthorize("hasRole('ENTRENADOR')")
    @DeleteMapping("/me/alumnos/{idAlumno}")
    public ResponseEntity<UsuarioResponse> eliminarAlumno(
            @AuthenticationPrincipal Usuario entrenador,
            @PathVariable Long idAlumno) {
        return ResponseEntity.ok(tutoriaService.eliminarAlumno(entrenador, idAlumno));
    }
}

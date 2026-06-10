package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.DTO.Request.LoginRequest;
import com.proyectoFinal.gymtracker.DTO.Request.UsuarioRequest;
import com.proyectoFinal.gymtracker.DTO.Response.AmigoResponse;
import com.proyectoFinal.gymtracker.DTO.Response.LoginResponse;
import com.proyectoFinal.gymtracker.DTO.Response.UsuarioResponse;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody UsuarioRequest usuarioRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(usuarioRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(usuarioService.login(loginRequest));
    }

    //Ver perfil
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> verPerfilPropio(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.verPerfilPropio(usuario));
    }

    @GetMapping("/{idUsuario}")
    public ResponseEntity<UsuarioResponse> verPerfilOtroUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(usuarioService.verPerfilOtroUsuario(idUsuario));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> editarPerfil(@AuthenticationPrincipal Usuario usuario,
                                                        @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.editarPerfil(usuario, request));
    }

    @PutMapping("/me/rutina-activa/{idRutina}")
    public ResponseEntity<UsuarioResponse> activarRutina(@AuthenticationPrincipal Usuario usuario,
                                                         @PathVariable Long idRutina) {
        return ResponseEntity.ok(usuarioService.activarRutina(usuario, idRutina));
    }

    @PostMapping("/me/amigos/{codigoAmigo}")
    public ResponseEntity<UsuarioResponse> agregarAmigo(@AuthenticationPrincipal Usuario usuario,
                                                        @PathVariable String codigoAmigo) {
        return ResponseEntity.ok(usuarioService.agregarAmigo(usuario, codigoAmigo));
    }

    @DeleteMapping("/me/amigos/{amigoId}")
    public ResponseEntity<UsuarioResponse> eliminarAmigo(@PathVariable Long amigoId,
                                                         @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.eliminarAmigo(amigoId, usuario));
    }

    @GetMapping("/me/amigos")
    public ResponseEntity<List<AmigoResponse>> getAmigos(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.getAmigos(usuario));
    }

    @GetMapping("/me/amigos/{amigoId}/perfil")
    public ResponseEntity<UsuarioResponse> getPerfilAmigo(@AuthenticationPrincipal Usuario usuario,
                                                     @PathVariable Long amigoId) {
        return ResponseEntity.ok(usuarioService.verPerfilAmigo(usuario, amigoId));
    }

    @PutMapping("/me/entrenador/{idEntrenador}")
    public ResponseEntity<UsuarioResponse> asignarEntrenador(@PathVariable Long idEntrenador,
                                                             @AuthenticationPrincipal Usuario usuario ) {
        return ResponseEntity.ok(usuarioService.asignarEntrenador(idEntrenador, usuario));
    }

    @DeleteMapping("/me/entrenador")
    public ResponseEntity<UsuarioResponse> eliminarEntrenador(@AuthenticationPrincipal Usuario usuario ) {
        return ResponseEntity.ok(usuarioService.eliminarEntrenador(usuario.getId()));
    }

    @GetMapping("/me/alumnos")
    public ResponseEntity<List<UsuarioResponse>> getAlumnos(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.getAlumnos(usuario.getId()));
    }

    @GetMapping("/me/entrenador")
    public ResponseEntity<UsuarioResponse> getEntrenadorActual(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(usuarioService.verEntrenadorActual(usuario.getId()));
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponse>> getUsuarios(@PageableDefault (size = 10) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.getUsuarios(pageable));
    }

}
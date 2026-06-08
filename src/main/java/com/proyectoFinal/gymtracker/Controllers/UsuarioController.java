package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.DTO.Request.LoginRequest;
import com.proyectoFinal.gymtracker.DTO.Request.UsuarioRequest;
import com.proyectoFinal.gymtracker.DTO.Response.AmigoResponse;
import com.proyectoFinal.gymtracker.DTO.Response.UsuarioResponse;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(usuarioService.login(loginRequest));
    }

    //agregado asi se ve el perfil propio
    @GetMapping("/{idUsuario}")
    public ResponseEntity<UsuarioResponse> getUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(usuarioService.getById(idUsuario));
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<UsuarioResponse> editarPerfil(@PathVariable Long idUsuario,
                                                        @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.editarPerfil(idUsuario, request));
    } //se va a recibir todo el usuario solo para tocar altura y peso
      //si les parece bien dejarlo asi, sino hay que crear otra clase request solo con 2 campos

    @PutMapping("/{idUsuario}/rutina-activa/{idRutina}")
    public ResponseEntity<UsuarioResponse> activarRutina(@PathVariable Long idUsuario, @PathVariable Long idRutina) {
        return ResponseEntity.ok(usuarioService.activarRutina(idUsuario, idRutina));
    }

    @PostMapping("/{idUsuario}/amigos/{codigoAmigo}")
    public ResponseEntity<UsuarioResponse> agregarAmigo(@PathVariable Long idUsuario,
                                                        @PathVariable String codigoAmigo) {
        return ResponseEntity.ok(usuarioService.agregarAmigo(idUsuario, codigoAmigo));
    }

    @GetMapping("/{idUsuario}/amigos")
    public ResponseEntity<List<AmigoResponse>> getAmigos(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(usuarioService.getAmigos(idUsuario));
    }

    @GetMapping("/{idUsuario}/amigos/{amigoId}/perfil")
    public ResponseEntity<UsuarioResponse> getPerfil(@PathVariable Long idUsuario, @PathVariable Long amigoId) {
        return ResponseEntity.ok(usuarioService.verPerfilAmigo(idUsuario, amigoId));
    }

    @PutMapping("/me/entrenador/{idEntrenador}")
    public ResponseEntity<UsuarioResponse> asignarEntrenador(@PathVariable Long idEntrenador,
                                                             @AuthenticationPrincipal Usuario usuario ) {
        return ResponseEntity.ok(usuarioService.asignarEntrenador(idEntrenador, usuario.getId()));
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

}
package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.DTO.Request.GimnasioRequest;
import com.proyectoFinal.gymtracker.DTO.Request.GimnasioUpdateRequest;
import com.proyectoFinal.gymtracker.DTO.Response.GimnasioResponse;
import com.proyectoFinal.gymtracker.DTO.Response.UsuarioResponse;
import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Services.GimnasioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gimnasios")
@RequiredArgsConstructor
public class GimnasioController {

    private final GimnasioService gimnasioService;

    @PostMapping
    public ResponseEntity<GimnasioResponse> createGimnasio(@Valid @RequestBody GimnasioRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(gimnasioService.createGimnasio(request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<GimnasioResponse>> getPropioGimnasios(@AuthenticationPrincipal Usuario usuario){
        return ResponseEntity.ok(gimnasioService.getPropioGimnasios(usuario));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GimnasioResponse> getGimnasio(@PathVariable Long id){
        return ResponseEntity.ok(gimnasioService.getGimnasioById(id));
    }

    @GetMapping
    public ResponseEntity<Page<GimnasioResponse>> getAllGimnasios(@PageableDefault(size = 10) Pageable pageable){
        return ResponseEntity.ok(gimnasioService.getAllGimnasios(pageable));
    }

    @PutMapping("/me")
    public ResponseEntity<GimnasioResponse> editarGimnasio(@Valid @RequestBody GimnasioUpdateRequest request,
                                                           @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(gimnasioService.actualizarGimnasio(request, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGimnasio(@PathVariable Long id){

        gimnasioService.eliminarGimnasio(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/me/suscripciones/{gimnasioid}")
    @PreAuthorize("hasRole('ADMIN_GIMNASIO')")
    public ResponseEntity<List<UsuarioResponse>> traerUsuarios (@AuthenticationPrincipal Usuario usuario,
                                                                @PathVariable Long gimnasioid){
        return ResponseEntity.ok(gimnasioService.traerUsuarios(usuario, gimnasioid));
    }
}

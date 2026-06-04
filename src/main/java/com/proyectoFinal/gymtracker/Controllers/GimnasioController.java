package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.DTO.Request.GimnasioRequest;
import com.proyectoFinal.gymtracker.DTO.Request.GimnasioUpdateRequest;
import com.proyectoFinal.gymtracker.DTO.Response.GimnasioResponse;
import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import com.proyectoFinal.gymtracker.Services.GimnasioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<GimnasioResponse>> getPropioGimnasios(){
        return ResponseEntity.ok(gimnasioService.getPropioGimnasios());
    }

    @GetMapping
    public ResponseEntity<Page<GimnasioResponse>> getAllGimnasios(@PageableDefault(size = 10) Pageable pageable){
        return ResponseEntity.ok(gimnasioService.getAllGimnasios(pageable));
    }

    @PutMapping("/asignar/{idGimnasio}")
    public ResponseEntity<GimnasioResponse> asignarGimnasio(@PathVariable Long idGimnasio) {
        return ResponseEntity.ok(gimnasioService.asignarGimnasio(idGimnasio));
    }

    @PutMapping("/me")
    public ResponseEntity<GimnasioResponse> editarGimnasio(@Valid @RequestBody GimnasioUpdateRequest request) {
        return ResponseEntity.ok(gimnasioService.actualizarGimnasio(request));
    }
}

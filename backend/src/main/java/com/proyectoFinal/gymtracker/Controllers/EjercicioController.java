package com.proyectoFinal.gymtracker.Controllers;


import com.proyectoFinal.gymtracker.DTO.Request.EjercicioRequest;
import com.proyectoFinal.gymtracker.DTO.Response.EjercicioResponse;
import com.proyectoFinal.gymtracker.Services.EjercicioService;
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
@RequestMapping("/ejercicios")
@RequiredArgsConstructor
public class EjercicioController {

    private final EjercicioService ejercicioService;

    @PostMapping
    public ResponseEntity<EjercicioResponse> addEjercicio(@Valid @RequestBody EjercicioRequest ejercicio){
        return ResponseEntity.status(HttpStatus.CREATED).body(ejercicioService.addEjercicio(ejercicio));
    }

    @PostMapping("/lote")
    public ResponseEntity<List<EjercicioResponse>> addEjercicios(@Valid @RequestBody List<EjercicioRequest> ejercicios){
        return ResponseEntity.status(HttpStatus.CREATED).body(ejercicioService.addEjercicios(ejercicios));
    }

    @PutMapping("/{idEjercicio}")
    public ResponseEntity<EjercicioResponse> updateEjercicio(@Valid @RequestBody EjercicioRequest ejercicio, @PathVariable Long idEjercicio){
        return ResponseEntity.status(HttpStatus.OK).body(ejercicioService.updateEjercicio(ejercicio, idEjercicio));
    }

    @DeleteMapping("/{idEjercicio}")
    public ResponseEntity<Void> deleteEjercicio(@PathVariable Long idEjercicio) {
        ejercicioService.deleteEjercicio(idEjercicio);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idEjercicio}")
    public ResponseEntity<EjercicioResponse> getEjercicio(@PathVariable Long idEjercicio) {
        return ResponseEntity.ok(ejercicioService.getById(idEjercicio));
    }

    @GetMapping
    public ResponseEntity<Page<EjercicioResponse>> getAllEjercicio(@PageableDefault (size = 10) Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(ejercicioService.getAll(pageable));
    }

    //buscar por x musculo, sea el principal o con que exista en el ejercicio suficiente
    @GetMapping("/porMusculo/{musculoId}")
    public ResponseEntity<List<EjercicioResponse>> getByMusculo(@PathVariable Long musculoId) {
        return ResponseEntity.ok(ejercicioService.getByMusculo(musculoId));
    }

    @GetMapping("/porMusculoPrincipal/{musculoId}")
    public ResponseEntity<List<EjercicioResponse>> getByMusculoPrincipal(@PathVariable Long musculoId) {
        return ResponseEntity.ok(ejercicioService.getByMusculoPrincipal(musculoId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<EjercicioResponse>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(ejercicioService.buscarPorNombre(nombre));
    }


}

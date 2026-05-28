package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.Modelo.Musculo;
import com.proyectoFinal.gymtracker.Services.MusculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/musculos")
@RequiredArgsConstructor
public class MusculoController {

    private final MusculoService musculoService;

    @PostMapping
    public ResponseEntity<Musculo> addMusculo(@RequestBody Musculo musculo){
        return ResponseEntity.status(HttpStatus.CREATED).body(musculoService.addMusculo(musculo));
    }

    @PostMapping("/lote")
    public ResponseEntity<List<Musculo>> addMusculos(@RequestBody List<Musculo> musculos){
        return ResponseEntity.status(HttpStatus.CREATED).body(musculoService.addMusculos(musculos));
    }

    @DeleteMapping("/{idMusculo}")
    public ResponseEntity<Void> deleteMusculos(@PathVariable Long idMusculo){
        musculoService.deleteMusculos(idMusculo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idMusculo}")
    public ResponseEntity<Musculo> getMusculo(@PathVariable Long idMusculo){
        return ResponseEntity.ok(musculoService.getMusculoById(idMusculo));
    }

    @GetMapping
    public ResponseEntity<List<Musculo>> getMusculos(){
        return ResponseEntity.status(HttpStatus.OK).body(musculoService.getMusculos());
    }

    @PutMapping
    public ResponseEntity<Musculo> updateNombreMusculo(@RequestBody Musculo musculo){
        return ResponseEntity.status(HttpStatus.OK).body(musculoService.updateMusculo(musculo));
    }
}

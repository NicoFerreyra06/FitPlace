package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.DTO.Request.MusculoRequest;
import com.proyectoFinal.gymtracker.DTO.Response.MusculoResponse;
import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import com.proyectoFinal.gymtracker.Modelo.Musculo;
import com.proyectoFinal.gymtracker.Services.MusculoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<MusculoResponse> addMusculo(@RequestBody MusculoRequest musculo){
        return ResponseEntity.status(HttpStatus.CREATED).body(musculoService.addMusculo(musculo));
    }

    @PostMapping("/lote")
    public ResponseEntity<List<MusculoResponse>> addMusculos(@RequestBody List<MusculoRequest> musculos){
        return ResponseEntity.status(HttpStatus.CREATED).body(musculoService.addMusculos(musculos));
    }

    @DeleteMapping("/{idMusculo}")
    public ResponseEntity<Void> deleteMusculos(@PathVariable Long idMusculo){
        musculoService.deleteMusculos(idMusculo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{idMusculo}")
    public ResponseEntity<MusculoResponse> getMusculo(@PathVariable Long idMusculo){
        return ResponseEntity.ok(musculoService.getMusculoById(idMusculo));
    }

    @GetMapping
    public ResponseEntity<Page<MusculoResponse>> getMusculos(@PageableDefault(size = 10) Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(musculoService.getMusculos(pageable));
    }

    @PutMapping("/{idMusculo}")
    public ResponseEntity<MusculoResponse> updateMusculo(@RequestBody MusculoRequest musculo,
                                                               @PathVariable Long idMusculo){
        return ResponseEntity.status(HttpStatus.OK).body(musculoService.updateMusculo(musculo, idMusculo));
    }

    @GetMapping("/grupoMuscular")
    public ResponseEntity<List<MusculoResponse>> getByGrupoMuscular(@RequestParam GrupoMuscular grupoMuscular){
        return ResponseEntity.status(HttpStatus.OK).body(musculoService.getByGrupoMuscular(grupoMuscular));
    }
}

package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.DTO.Request.RutinaRequest;
import com.proyectoFinal.gymtracker.DTO.Response.DiaRutinaResponse;
import com.proyectoFinal.gymtracker.DTO.Response.RutinaResponse;
import com.proyectoFinal.gymtracker.Services.RutinaService;
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
@RequestMapping("/rutinas")
@RequiredArgsConstructor
public class RutinaController {

    private final RutinaService rutinaService;

    @PostMapping
    public ResponseEntity<RutinaResponse> createRutina(@Valid @RequestBody RutinaRequest rutinaRequest){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rutinaService.createRutina(rutinaRequest));
    }

    @PutMapping("/{idRutina}")
    public ResponseEntity<RutinaResponse> updateRutina(@Valid @RequestBody RutinaRequest rutinaRequest,
                                                       @PathVariable Long idRutina){
        return ResponseEntity.status(HttpStatus.OK).body(rutinaService.updateRutina(rutinaRequest,idRutina));
    }

    @GetMapping("/{idRutina}")
    public ResponseEntity<RutinaResponse> getRutinaById(@PathVariable Long idRutina){
        return ResponseEntity.status(HttpStatus.OK).body(rutinaService.getRutinaById(idRutina));
    }

    @GetMapping
    public ResponseEntity<Page<RutinaResponse>> getAllRutinas(@PageableDefault(size = 10) Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(rutinaService.getAllRutinas(pageable));
    }

    @DeleteMapping("/{idRutina}")
    public ResponseEntity<Void> deleteRutinaById(@PathVariable Long idRutina){
        rutinaService.deleteRutina(idRutina);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{idUsuario}/hoy")
    public ResponseEntity<DiaRutinaResponse> getDiaActual(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(rutinaService.getDiaRutinaActual(idUsuario));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<RutinaResponse>> getRutinaByUsuario(@PathVariable Long idUsuario){
        return ResponseEntity.ok(rutinaService.getRutinasByIdUsuario(idUsuario));
    }

}

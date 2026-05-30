package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.DTO.Request.EntrenamientoLogRequest;
import com.proyectoFinal.gymtracker.DTO.Response.EntrenamientoLogResponse;
import com.proyectoFinal.gymtracker.DTO.Response.HistorialEjercicioResponse;
import com.proyectoFinal.gymtracker.Services.EntrenamientoLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/entrenamientos")
@RequiredArgsConstructor
public class EntrenamientoLogController {

    private final EntrenamientoLogService entrenamientoLogService;

    @PostMapping
    public ResponseEntity<EntrenamientoLogResponse> addEntrenamientoLog (@Valid @RequestBody EntrenamientoLogRequest entrenamientoLogRequest){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entrenamientoLogService.addEntrenamientoLog(entrenamientoLogRequest));
    }

    @PutMapping("/{idEntrenamiento}")
    public ResponseEntity<EntrenamientoLogResponse> updateEntrenamiento(@Valid @RequestBody EntrenamientoLogRequest entrenamientoLogRequest,
                                                                        @PathVariable Long idEntrenamiento){
        return ResponseEntity.status(HttpStatus.OK)
                .body(entrenamientoLogService.updateEntrenamiento(entrenamientoLogRequest, idEntrenamiento));
    }

    @GetMapping("/{idEntrenamiento}")
    public ResponseEntity<EntrenamientoLogResponse> getEntrenamientoLogById(@PathVariable Long idEntrenamiento){
        return ResponseEntity.status(HttpStatus.OK)
                .body(entrenamientoLogService.getEntrenamientoLogById(idEntrenamiento));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<EntrenamientoLogResponse>> getEntrenamientoLogByUsuario(@PathVariable Long idUsuario){
        return ResponseEntity.status(HttpStatus.OK)
                .body(entrenamientoLogService.getEntrenamientoLogByUsuarioId(idUsuario));
    }

    @GetMapping("/usuario/{idUsuario}/ejercicio/{idEjercicio}")
    public ResponseEntity<Map<String, List<HistorialEjercicioResponse>>> getHistorialEjercicio(
            @PathVariable Long idUsuario,
            @PathVariable Long idEjercicio
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(entrenamientoLogService.getHistorialEjercicio(idUsuario, idEjercicio));
    }

    @DeleteMapping("/{idEntrenamiento}")
    public ResponseEntity<Void> deleteEntrenamientoLog(@PathVariable Long idEntrenamiento){
        entrenamientoLogService.deleteEntrenamientoLog(idEntrenamiento);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}

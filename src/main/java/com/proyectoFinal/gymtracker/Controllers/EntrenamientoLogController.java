package com.proyectoFinal.gymtracker.Controllers;

import com.proyectoFinal.gymtracker.DTO.Request.EntrenamientoLogRequest;
import com.proyectoFinal.gymtracker.DTO.Response.EntrenamientoLogResponse;
import com.proyectoFinal.gymtracker.DTO.Response.HistorialEjercicioResponse;
import com.proyectoFinal.gymtracker.DTO.Response.VolumenSemanalResponse;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Services.EntrenamientoLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/entrenamientos")
@RequiredArgsConstructor
public class EntrenamientoLogController {

    private final EntrenamientoLogService entrenamientoLogService;

    @PostMapping
    public ResponseEntity<EntrenamientoLogResponse> addEntrenamientoLog (@Valid @RequestBody EntrenamientoLogRequest entrenamientoLogRequest,
                                                                         @AuthenticationPrincipal Usuario usuarioLogueado){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(entrenamientoLogService.addEntrenamientoLog(entrenamientoLogRequest, usuarioLogueado));
    }

    @PutMapping("/{idEntrenamiento}")
    public ResponseEntity<EntrenamientoLogResponse> updateEntrenamiento(@Valid @RequestBody EntrenamientoLogRequest entrenamientoLogRequest,
                                                                        @PathVariable Long idEntrenamiento,
                                                                        @AuthenticationPrincipal Usuario usuarioLogueado){
        return ResponseEntity.status(HttpStatus.OK)
                .body(entrenamientoLogService.updateEntrenamiento(entrenamientoLogRequest, idEntrenamiento, usuarioLogueado));
    }

    @GetMapping("/{idEntrenamiento}")
    public ResponseEntity<EntrenamientoLogResponse> getEntrenamientoLogById(@PathVariable Long idEntrenamiento){
        return ResponseEntity.status(HttpStatus.OK)
                .body(entrenamientoLogService.getEntrenamientoLogById(idEntrenamiento));
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<Page<EntrenamientoLogResponse>> getEntrenamientos(@PageableDefault(size = 10) Pageable pageable,
                                                                            @PathVariable Long idUsuario,
                                                                            @RequestParam (required = false) LocalDate desde,
                                                                            @RequestParam (required = false) LocalDate hasta,
                                                                            @AuthenticationPrincipal Usuario usuarioLogueado) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(entrenamientoLogService.getEntrenamientos(idUsuario, pageable, desde, hasta, usuarioLogueado));
    }

    @GetMapping("/usuario/{idUsuario}/ejercicio/{idEjercicio}/progresion")
    public ResponseEntity<List<HistorialEjercicioResponse>> getProgresionEjercicio(
            @PathVariable Long idUsuario,
            @PathVariable Long idEjercicio,
            @AuthenticationPrincipal Usuario usuarioLogueado) {
        return ResponseEntity.ok(
                entrenamientoLogService.getProgrecionEjercicioPremium(idUsuario, idEjercicio, usuarioLogueado));
    }

    @GetMapping("/usuario/{idUsuario}/volumen-semanal")
    public ResponseEntity<List<VolumenSemanalResponse>> getVolumenSemanal(
            @PathVariable Long idUsuario,
            @AuthenticationPrincipal Usuario usuarioLogueado) {
        return ResponseEntity.ok(
                entrenamientoLogService.getVolumenSemanal(idUsuario, usuarioLogueado));
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
    public ResponseEntity<Void> deleteEntrenamientoLog(@PathVariable Long idEntrenamiento,
                                                       @AuthenticationPrincipal Usuario usuario){
        entrenamientoLogService.deleteEntrenamientoLog(idEntrenamiento, usuario);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}

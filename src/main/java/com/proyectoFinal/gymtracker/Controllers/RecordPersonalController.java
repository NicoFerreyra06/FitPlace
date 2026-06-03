package com.proyectoFinal.gymtracker.Controllers;


import com.proyectoFinal.gymtracker.DTO.Response.RecordPersonalResponse;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Services.RecordPersonalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class RecordPersonalController {

    private final RecordPersonalService recordPersonalService;

    @GetMapping("/ejercicio/{ejercicioId}")
    public ResponseEntity<RecordPersonalResponse> getRecordPersonalByEjercicioId(
            @PathVariable Long ejercicioId,
            @AuthenticationPrincipal Usuario usuarioAutenticado) {
        return ResponseEntity.ok(recordPersonalService.getRecordPersonalByEjercicioId(
                        usuarioAutenticado.getId(),
                        ejercicioId)
        );
    }

    //ver records de cada ejercicio de x usuario
    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<RecordPersonalResponse>> getRecordsPersonalesByUsuarioId(@PathVariable Long id) {
        return ResponseEntity.ok(
                recordPersonalService.getRecordsPersonalesByUsuarioId(id));
    }

    //ver ranking de records de todos los usuarios en x ejercicio
    @GetMapping("/ranking/{ejercicioId}")
    public ResponseEntity<Page<RecordPersonalResponse>> getRankingRecordsPersonalesByEjercicioId(@PageableDefault (size = 10) Pageable pageable, @PathVariable Long ejercicioId) {
        return ResponseEntity.ok(recordPersonalService.getRankingRecordsPersonalesByEjercicioId(pageable,ejercicioId));
    }

}

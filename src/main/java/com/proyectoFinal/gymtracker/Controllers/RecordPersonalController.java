package com.proyectoFinal.gymtracker.Controllers;


import com.proyectoFinal.gymtracker.Modelo.RecordPersonal;
import com.proyectoFinal.gymtracker.Services.RecordPersonalService;
import com.proyectoFinal.gymtracker.Services.UsuarioService;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
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
    public RecordPersonal getRecordPersonalByEjercicioId(@PathVariable Long ejercicioId) {

        //puesto asi hasta ver jwt y sacar el usuario del token
        return recordPersonalService.getRecordPersonalByEjercicioId(null, ejercicioId);

    }

    //ver records de cada ejercicio de x usuario
    @GetMapping("/usuario/{id}")
    public List<RecordPersonal> getRecordsPersonalesByUsuarioId(@PathVariable Long id) {
        return recordPersonalService.getRecordsPersonalesByUsuarioId(id);
    }


    //ver ranking de records de todos los usuarios en x ejercicio
    @GetMapping("/ranking/{ejercicioId}")
    public List<RecordPersonal> getRankingRecordsPersonalesByEjercicioId(@PathVariable Long ejercicioId){
        return recordPersonalService.getRankingRecordsPersonalesByEjercicioId(ejercicioId);
    }

}

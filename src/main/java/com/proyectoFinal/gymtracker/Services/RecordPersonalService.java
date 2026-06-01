package com.proyectoFinal.gymtracker.Services;


import com.proyectoFinal.gymtracker.Modelo.Ejercicio;
import com.proyectoFinal.gymtracker.Modelo.RecordPersonal;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.RecordPersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordPersonalService {

    private final RecordPersonalRepository recordPersonalRepository;


    public RecordPersonal toResponse(RecordPersonal recordPersonal){
        return RecordPersonal.builder()
                .id(recordPersonal.getId())
                .usuario(recordPersonal.getUsuario())
                .pesoMaximo(recordPersonal.getPesoMaximo())
                .fechaLogro(recordPersonal.getFechaLogro())
                .build();
    }


    // Muestra el record personal en 1 ejercicio de 1 usuario.
    public RecordPersonal getRecordPersonalByEjercicioId(Long usuarioId, Long ejercicioId) {
        return recordPersonalRepository.findByUsuarioIdAndEjercicioId(usuarioId, ejercicioId);
    }

    // Muestra los record personales en todos los ejercicios de 1 usuario.
    public List<RecordPersonal> getRecordsPersonalesByUsuarioId(Long usuarioId) {
        return recordPersonalRepository.findRecordPersonalByUsuarioId(usuarioId);
    }

    // Muestra el ranking de records personales de todos los usuarios.
    public List<RecordPersonal> getRankingRecordsPersonalesByEjercicioId(Long ejercicioId) {
        List<RecordPersonal> ranking = recordPersonalRepository.findByEjercicioIdOrderByPesoMaximoDesc(ejercicioId);

        return ranking.stream()
                .map(this::toResponse)
                .toList();
    }

    //este sin endpoint, lo llama el service de EntrenamientoLog
    public void actualizarRecordSiCorresponde(Usuario usuario, Ejercicio ejercicio, Double pesoNuevo) {
        RecordPersonal recordExistente = recordPersonalRepository
                .findByUsuarioIdAndEjercicioId(usuario.getId(), ejercicio.getId());

        if (recordExistente == null) {
            RecordPersonal nuevo = RecordPersonal.builder()
                    .usuario(usuario)
                    .ejercicio(ejercicio)
                    .pesoMaximo(pesoNuevo)
                    .fechaLogro(LocalDate.now())
                    .build();
            recordPersonalRepository.save(nuevo);

        } else if (pesoNuevo > recordExistente.getPesoMaximo()) {
            recordExistente.setPesoMaximo(pesoNuevo);
            recordExistente.setFechaLogro(LocalDate.now());
            recordPersonalRepository.save(recordExistente);

        } else if (pesoNuevo.equals(recordExistente.getPesoMaximo())) {
            recordExistente.setFechaLogro(LocalDate.now());
            recordPersonalRepository.save(recordExistente);
        }
    }
}

package com.proyectoFinal.gymtracker.Services;


import com.proyectoFinal.gymtracker.DTO.Response.RecordPersonalResponse;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
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


    private RecordPersonalResponse toResponse(RecordPersonal record) {
        return RecordPersonalResponse.builder()
                .id(record.getId())
                .ejercicioId(record.getEjercicio().getId())
                .nombreUsuario(record.getUsuario().getUsername())
                .nombreEjercicio(record.getEjercicio().getNombre())
                .pesoMaximo(record.getPesoMaximo())
                .fechaLogro(record.getFechaLogro())
                .build();

    }


    // Muestra el record personal en 1 ejercicio de 1 usuario.
    public RecordPersonalResponse getRecordPersonalByEjercicioId(Long usuarioId, Long ejercicioId) {
        RecordPersonal record = recordPersonalRepository.findByUsuarioIdAndEjercicioId(usuarioId, ejercicioId);

        if (record == null) {
            throw new ResourceNotFoundException("No tenés record en ese ejercicio");
        }
        return toResponse(record);
    }

    // Muestra los record personales en todos los ejercicios de 1 usuario.
    public List<RecordPersonalResponse> getRecordsPersonalesByUsuarioId(Long usuarioId) {
        return recordPersonalRepository.findRecordPersonalByUsuarioId(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Muestra el ranking de records personales de todos los usuarios.
    public List<RecordPersonalResponse> getRankingRecordsPersonalesByEjercicioId(Long ejercicioId) {
        return recordPersonalRepository.findByEjercicioIdOrderByPesoMaximoDesc(ejercicioId)
                .stream()
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

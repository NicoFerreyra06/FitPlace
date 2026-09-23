package com.proyectoFinal.gymtracker.Services;


import com.proyectoFinal.gymtracker.DTO.Response.RecordPersonalResponse;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Ejercicio;
import com.proyectoFinal.gymtracker.Modelo.RecordPersonal;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.RecordPersonalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.proyectoFinal.gymtracker.Modelo.MarcaEjercicio;
import com.proyectoFinal.gymtracker.Repositories.MarcaEjercicioRepository;

@Service
@RequiredArgsConstructor
public class RecordPersonalService {

    private final RecordPersonalRepository recordPersonalRepository;
    private final MarcaEjercicioRepository marcaEjercicioRepository;


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
    public List<RecordPersonalResponse> getRecordsPersonalesByUsuarioId(Long usuarioId, Usuario authUser) {
        if (!authUser.getId().equals(usuarioId) && authUser.getRol() != Rol.ADMIN) {
            throw new BusinessLogicException("No tienes permisos para ver estos records");
        }
        return recordPersonalRepository.findRecordPersonalByUsuarioId(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Muestra el ranking de records personales de todos los usuarios.
    public Page<RecordPersonalResponse> getRankingRecordsPersonalesByEjercicioId(Pageable pageable, Long ejercicioId) {
        return recordPersonalRepository.findByEjercicioIdOrderByPesoMaximoDesc(pageable,ejercicioId)
                .map(this::toResponse);
    }

    //este sin endpoint, lo llama el service de EntrenamientoLog
    public void recalcularRecord(Usuario usuario, Ejercicio ejercicio) {
        RecordPersonal recordExistente = recordPersonalRepository
                .findByUsuarioIdAndEjercicioId(usuario.getId(), ejercicio.getId());

        Optional<MarcaEjercicio> topMarca = 
                marcaEjercicioRepository.findFirstByEntrenamientoLog_Usuario_IdAndEjercicioRutina_Ejercicio_IdOrderByPesoLevantadoDesc(usuario.getId(), ejercicio.getId());

        if (topMarca.isPresent()) {
            MarcaEjercicio marca = topMarca.get();
            if (recordExistente == null) {
                RecordPersonal nuevo = RecordPersonal.builder()
                        .usuario(usuario)
                        .ejercicio(ejercicio)
                        .pesoMaximo(marca.getPesoLevantado())
                        .fechaLogro(marca.getEntrenamientoLog().getFecha())
                        .build();
                recordPersonalRepository.save(nuevo);
            } else {
                recordExistente.setPesoMaximo(marca.getPesoLevantado());
                recordExistente.setFechaLogro(marca.getEntrenamientoLog().getFecha());
                recordPersonalRepository.save(recordExistente);
            }
        } else {
            if (recordExistente != null) {
                recordPersonalRepository.delete(recordExistente);
            }
        }
    }

}

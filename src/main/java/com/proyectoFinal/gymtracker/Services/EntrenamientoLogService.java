package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.EntrenamientoLogRequest;
import com.proyectoFinal.gymtracker.DTO.Response.EntrenamientoLogResponse;
import com.proyectoFinal.gymtracker.DTO.Response.HistorialEjercicioResponse;
import com.proyectoFinal.gymtracker.DTO.Response.MarcaEjercicioResponse;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Exception.UserNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.*;
import com.proyectoFinal.gymtracker.Repositories.*;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EntrenamientoLogService {

    private final EntrenamientoLogRepository entrenamientoLogRepository;
    private final UsuarioRepository usuarioRepository;
    private final RutinaRepository rutinaRepository;
    private final EjercicioRutinaRepository ejercicioRutinaRepository;
    private final EjercicioRepository ejercicioRepository;

    @Transactional
    public EntrenamientoLogResponse addEntrenamientoLog (@Valid EntrenamientoLogRequest entrenamientoLogRequest) {
        Usuario user = usuarioRepository.findById(entrenamientoLogRequest.getIdUsuario())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        Rutina rutina = rutinaRepository.findById(entrenamientoLogRequest.getIdRutina())
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));

        EntrenamientoLog entrenamientoLog = EntrenamientoLog.builder()
                .usuario(user).fecha(LocalDate.now()).rutinaEjecutada(rutina).build();

        List<MarcaEjercicio> marcaEjercicioList = entrenamientoLogRequest.getMarcasEjercicio()
                .stream().map(marca -> {
                    EjercicioRutina ejercicioRutina = ejercicioRutinaRepository
                            .findById(marca.getEjercicioRutinaId()).orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrada"));

                    return MarcaEjercicio.builder()
                            .pesoLevantado(marca.getPesoLevantado())
                            .repeticionesLogradas(marca.getRepeticionesLogradas())
                            .entrenamientoLog(entrenamientoLog)
                            .ejercicioRutina(ejercicioRutina).build();
                }).toList();

        entrenamientoLog.setMarcas(marcaEjercicioList);

        EntrenamientoLog entrenamientoLogSaved = entrenamientoLogRepository.save(entrenamientoLog);

        return mapEntrenamientoLogResponse(entrenamientoLogSaved);
    }

    public EntrenamientoLogResponse getEntrenamientoLogById (Long idEntrenamientoLog) {
        EntrenamientoLog entrenamientoLog = entrenamientoLogRepository.findById(idEntrenamientoLog)
                .orElseThrow(()-> new ResourceNotFoundException("Entrenamiento no encontrado"));

        return mapEntrenamientoLogResponse(entrenamientoLog);
    }

    public List<EntrenamientoLogResponse> getEntrenamientoLogByUsuarioId(Long idUsuario) {
        Usuario usuario =  usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        List<EntrenamientoLog> entrenamientosUsuario = entrenamientoLogRepository.findByUsuarioId(usuario.getId());

        if (entrenamientosUsuario.isEmpty()) {
            return List.of();
        }

        return entrenamientosUsuario.stream()
                .map(this::mapEntrenamientoLogResponse).toList();
    }

    public Map<String, List<HistorialEjercicioResponse>> getHistorialEjercicio(Long idUsuario, Long idEjercicio) {
        Ejercicio ejercicio = ejercicioRepository.findById(idEjercicio)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));
        return Map.of(ejercicio.getNombre(), entrenamientoLogRepository.historialEjercicio(idUsuario, idEjercicio));
    }

    // === Metodos auxiliares ===

    private EntrenamientoLogResponse mapEntrenamientoLogResponse(EntrenamientoLog entrenamientoLog) {
        return EntrenamientoLogResponse.builder()
                .id(entrenamientoLog.getId())
                .nombreUsuario(entrenamientoLog.getUsuario().getUsername())
                .nombreRutina(entrenamientoLog.getRutinaEjecutada().getNombre())
                .fecha(entrenamientoLog.getFecha())
                .marcasEjercicio(entrenamientoLog.getMarcas()
                        .stream().map(this::mapMarcaEjercicioResponse).toList())
                .build();
    }

    private MarcaEjercicioResponse mapMarcaEjercicioResponse(MarcaEjercicio marcaEjercicio) {
        return MarcaEjercicioResponse.builder()
                .id(marcaEjercicio.getId())
                .nombreEjercicio(marcaEjercicio.getEjercicioRutina().getEjercicio().getNombre())
                .pesoLevantado(marcaEjercicio.getPesoLevantado())
                .repeticionesLogradas(marcaEjercicio.getRepeticionesLogradas()).build();
    }
}

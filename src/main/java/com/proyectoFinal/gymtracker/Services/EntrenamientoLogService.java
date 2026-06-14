package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.EntrenamientoLogRequest;
import com.proyectoFinal.gymtracker.DTO.Response.EntrenamientoLogResponse;
import com.proyectoFinal.gymtracker.DTO.Response.HistorialEjercicioResponse;
import com.proyectoFinal.gymtracker.DTO.Response.MarcaEjercicioResponse;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Exception.UserNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.*;
import com.proyectoFinal.gymtracker.Repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final RecordPersonalService  recordPersonalService;
    private static final int LIMITE_HISTORIAL_DIAS = 30;

    @Transactional
    public EntrenamientoLogResponse addEntrenamientoLog (EntrenamientoLogRequest entrenamientoLogRequest,
                                                         Usuario usuarioLogueado) {

        Rutina rutina = rutinaRepository.findById(entrenamientoLogRequest.getIdRutina())
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));

        EntrenamientoLog entrenamientoLog = EntrenamientoLog.builder()
                .usuario(usuarioLogueado).fecha(LocalDate.now()).rutinaEjecutada(rutina).build();

        List<MarcaEjercicio> marcaEjercicioList = entrenamientoLogRequest.getMarcasEjercicio()
                .stream().map(marca -> {
                    EjercicioRutina ejercicioRutina = ejercicioRutinaRepository
                            .findById(marca.getEjercicioRutinaId())
                            .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrada"));

                    if (!ejercicioRutina.getDia().getRutina().getId().equals(rutina.getId())) {
                        throw new BusinessLogicException("El ejercicio no pertenece a la rutina ejecutada");
                    }

                    //para actualizar el record automaticamente
                    recordPersonalService.actualizarRecordSiCorresponde(
                            usuarioLogueado,
                            ejercicioRutina.getEjercicio(),
                            marca.getPesoLevantado(),
                            entrenamientoLog.getFecha()
                    );

                    return MarcaEjercicio.builder()
                            .pesoLevantado(marca.getPesoLevantado())
                            .repeticionesLogradas(marca.getRepeticionesLogradas())
                            .entrenamientoLog(entrenamientoLog)
                            .ejercicioRutina(ejercicioRutina).build();
                }).toList();

        entrenamientoLog.setMarcas(marcaEjercicioList);

        EntrenamientoLog saved = entrenamientoLogRepository.save(entrenamientoLog);

        return mapEntrenamientoLogResponse(saved);

    }

    @Transactional
    public EntrenamientoLogResponse updateEntrenamiento(EntrenamientoLogRequest entrenamientoLogRequest,
                                                        Long idEntrenamiento,
                                                        Usuario usuarioLogueado) {

        EntrenamientoLog entrenamientoExistente = entrenamientoLogRepository.findById(idEntrenamiento)
                .orElseThrow(() -> new ResourceNotFoundException("Entrenamiento no encontrado"));

        if (!entrenamientoExistente.getUsuario().getId().equals(usuarioLogueado.getId())) {
            throw new BusinessLogicException("No tenés permiso para editar un entrenamiento que no es tuyo");
        }

        LocalDate fechaEntrenamiento = entrenamientoExistente.getFecha();
        LocalDate unaSemana = LocalDate.now().minusDays(7);

        if (fechaEntrenamiento.isBefore(unaSemana)) {
            throw new BusinessLogicException("Solo podés editar entrenamientos de hace una semana maximo");
        }

        Rutina rutina = rutinaRepository.findById(entrenamientoLogRequest.getIdRutina())
                        .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));


        entrenamientoExistente.setRutinaEjecutada(rutina);
        entrenamientoExistente.getMarcas().clear();

        List<MarcaEjercicio> nuevasMarcas = entrenamientoLogRequest.getMarcasEjercicio()
                .stream().map(marcaEjercicioRequest -> {
                    EjercicioRutina ejercicioRutina = ejercicioRutinaRepository.findById(marcaEjercicioRequest.getEjercicioRutinaId())
                            .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

                    if (!ejercicioRutina.getDia().getRutina().getId().equals(rutina.getId())) {
                        throw new BusinessLogicException("El ejercicio no pertenece a la rutina ejecutada");
                    }

                    recordPersonalService.actualizarRecordSiCorresponde(
                            usuarioLogueado,
                            ejercicioRutina.getEjercicio(),
                            marcaEjercicioRequest.getPesoLevantado(),
                            entrenamientoExistente.getFecha()
                    );

                    return MarcaEjercicio.builder()
                            .pesoLevantado(marcaEjercicioRequest.getPesoLevantado())
                            .repeticionesLogradas(marcaEjercicioRequest.getRepeticionesLogradas())
                            .entrenamientoLog(entrenamientoExistente)
                            .ejercicioRutina(ejercicioRutina).build();
                }).toList();

        entrenamientoExistente.getMarcas().addAll(nuevasMarcas);
        EntrenamientoLog entrenamientoSaved = entrenamientoLogRepository.save(entrenamientoExistente);

        return mapEntrenamientoLogResponse(entrenamientoSaved);
    }

    public EntrenamientoLogResponse getEntrenamientoLogById (Long idEntrenamientoLog) {
        EntrenamientoLog entrenamientoLog = entrenamientoLogRepository.findById(idEntrenamientoLog)
                .orElseThrow(()-> new ResourceNotFoundException("Entrenamiento no encontrado"));

        return mapEntrenamientoLogResponse(entrenamientoLog);
    }

    public Page<EntrenamientoLogResponse> getEntrenamientos(Long idUsuario, Pageable pageable, LocalDate desde, LocalDate hasta, Usuario usuarioLogueado) {
        Usuario usuario =  usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (usuarioLogueado.getRol().equals(Rol.USUARIO)) {
            LocalDate limiteBasico = LocalDate.now().minusDays(LIMITE_HISTORIAL_DIAS);

            if (desde == null || desde.isBefore(limiteBasico)) {
                desde = limiteBasico;
            }
        }
        Page<EntrenamientoLog> entrenamientosUsuario = entrenamientoLogRepository.findByUsuarioIdAndFechas(usuario.getId(), desde, hasta, pageable);

        return entrenamientosUsuario.map(this::mapEntrenamientoLogResponse);
    }

    public Map<String, List<HistorialEjercicioResponse>> getHistorialEjercicio(Long idUsuario, Long idEjercicio) {
        Ejercicio ejercicio = ejercicioRepository.findById(idEjercicio)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));
        return Map.of(ejercicio.getNombre(), entrenamientoLogRepository.historialEjercicio(idUsuario, idEjercicio));
    }

    public void deleteEntrenamientoLog (Long idEntrenamientoLog, Usuario usuario) {

        EntrenamientoLog entrenamientoLog = entrenamientoLogRepository.findById(idEntrenamientoLog)
                .orElseThrow(() -> new ResourceNotFoundException("Entrenamiento no encontrado"));

        if (!entrenamientoLog.getUsuario().getId().equals(usuario.getId())) throw new UserNotFoundException("Usted no es duenio de este entrenamiento");

        entrenamientoLogRepository.delete(entrenamientoLog);
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

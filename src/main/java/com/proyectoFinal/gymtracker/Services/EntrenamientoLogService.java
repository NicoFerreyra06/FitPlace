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

    @Transactional
    public EntrenamientoLogResponse addEntrenamientoLog (EntrenamientoLogRequest entrenamientoLogRequest,
                                                         Usuario usuarioLogueado) {

        Rutina rutina = rutinaRepository.findById(entrenamientoLogRequest.getIdRutina())
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));

        EntrenamientoLog ultimoEntrenamiento = entrenamientoLogRepository
                .findFirstByUsuarioIdOrderByFechaDesc(usuarioLogueado.getId());
        LocalDate fechaUltimo = ultimoEntrenamiento != null ? ultimoEntrenamiento.getFecha() : null;
        LocalDate hoy = LocalDate.now();

        EntrenamientoLog entrenamientoLog = EntrenamientoLog.builder()
                .usuario(usuarioLogueado).fecha(hoy).rutinaEjecutada(rutina).build();

        List<MarcaEjercicio> marcaEjercicioList = entrenamientoLogRequest.getMarcasEjercicio()
                .stream().map(marca -> {
                    EjercicioRutina ejercicioRutina = ejercicioRutinaRepository
                            .findById(marca.getEjercicioRutinaId())
                            .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

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

        registrarRacha(usuarioLogueado, fechaUltimo, hoy);

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

        if (!usuarioLogueado.getId().equals(idUsuario) && usuarioLogueado.getRol() != Rol.ADMIN) {
            throw new BusinessLogicException("No tienes permisos para ver estos entrenamientos");
        }

        Page<EntrenamientoLog> entrenamientosUsuario = entrenamientoLogRepository.findByUsuarioIdAndFechas(idUsuario, desde, hasta, pageable);

        return entrenamientosUsuario.map(this::mapEntrenamientoLogResponse);
    }

    public Page<EntrenamientoLogResponse> getEntrenamientosDeAlumno(Pageable pageable, LocalDate desde, LocalDate hasta, Long idAlumno, Usuario entrenadorLogueado) {
        Usuario alumno = usuarioRepository.findById(idAlumno)
                .orElseThrow(() -> new UserNotFoundException("Alumno no encontrado"));

        if (!entrenadorLogueado.getRol().equals(Rol.ENTRENADOR)) {
            throw new BusinessLogicException("No tienes rol de ENTRENADOR");
        }

        if (alumno.getEntrenador() == null || !alumno.getEntrenador().getId().equals(entrenadorLogueado.getId())) {
            throw new BusinessLogicException("Este alumno no está a tu cargo");
        }

        Page<EntrenamientoLog> entrenamientosUsuario = entrenamientoLogRepository.findByUsuarioIdAndFechas(idAlumno, desde, hasta, pageable);
        return entrenamientosUsuario.map(this::mapEntrenamientoLogResponse);
    }

    public Map<String, List<HistorialEjercicioResponse>> getHistorialEjercicio(Long idUsuario, Long idEjercicio, Usuario actor) {
        Ejercicio ejercicio = ejercicioRepository.findById(idEjercicio)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

        boolean esElMismo = actor.getId().equals(idUsuario);
        boolean esAdmin = actor.getRol() == Rol.ADMIN;
        boolean esSuEntrenador = false;

        if (!esElMismo && !esAdmin && actor.getRol() == Rol.ENTRENADOR) {
            Usuario alumno = usuarioRepository.findById(idUsuario).orElse(null);
            if (alumno != null && alumno.getEntrenador() != null && alumno.getEntrenador().getId().equals(actor.getId())) {
                esSuEntrenador = true;
            }
        }

        if (!esElMismo && !esAdmin && !esSuEntrenador) {
            throw new BusinessLogicException("Sin permisos");
        }
        
        return Map.of(ejercicio.getNombre(), entrenamientoLogRepository.historialEjercicio(idUsuario, idEjercicio));
    }

    public void deleteEntrenamientoLog (Long idEntrenamientoLog, Usuario usuario) {

        EntrenamientoLog entrenamientoLog = entrenamientoLogRepository.findById(idEntrenamientoLog)
                .orElseThrow(() -> new ResourceNotFoundException("Entrenamiento no encontrado"));

        if (!entrenamientoLog.getUsuario().getId().equals(usuario.getId())) throw new UserNotFoundException("Usted no es duenio de este entrenamiento");

        entrenamientoLogRepository.delete(entrenamientoLog);
    }

    private void registrarRacha(Usuario usuarioLogueado, LocalDate fechaUltimo, LocalDate fechaHoy) {
        int rachaActual = (usuarioLogueado.getRachaActualDias() != null) ? usuarioLogueado.getRachaActualDias() : 0;
        int rachaMaxima = (usuarioLogueado.getRachaMaximaDias() != null) ? usuarioLogueado.getRachaMaximaDias() : 0;

        // Si ya registró entrenamiento hoy, no hacer nada para evitar doble conteo
        if (fechaUltimo != null && fechaUltimo.equals(fechaHoy)) {
            return;
        }

        Rutina rutinaActiva = usuarioLogueado.getRutinaActiva();
        List<java.time.DayOfWeek> diasProgramados = (rutinaActiva != null && rutinaActiva.getDias() != null)
                ? rutinaActiva.getDias().stream().map(DiaRutina::getDiaDeLaSemana).toList()
                : List.of();

        boolean rachaRota = false;

        // Verificar si faltó a algún día programado desde su último entrenamiento
        if (fechaUltimo != null) {
            LocalDate current = fechaUltimo.plusDays(1);
            while (current.isBefore(fechaHoy)) {
                boolean debiaEntrenar = false;
                if (!diasProgramados.isEmpty()) {
                    debiaEntrenar = diasProgramados.contains(current.getDayOfWeek());
                } else {
                    debiaEntrenar = current.getDayOfWeek() != java.time.DayOfWeek.SUNDAY;
                }

                if (debiaEntrenar) {
                    rachaRota = true;
                    break;
                }
                current = current.plusDays(1);
            }
        }

        if (rachaRota) {
            rachaActual = 0;
        }

        // Evaluar si hoy es un día de cumplimiento
        boolean sumaRacha = false;
        if (!diasProgramados.isEmpty()) {
            sumaRacha = diasProgramados.contains(fechaHoy.getDayOfWeek());
        } else {
            sumaRacha = fechaHoy.getDayOfWeek() != java.time.DayOfWeek.SUNDAY;
        }

        // Incrementa si es día de cumplimiento, o si la racha está en 0 (para al menos arrancar en 1)
        if (sumaRacha || rachaActual == 0) {
            rachaActual++;
        }

        if (rachaActual > rachaMaxima) {
            rachaMaxima = rachaActual;
            usuarioLogueado.setRachaMaximaDias(rachaMaxima);
        }

        usuarioLogueado.setRachaActualDias(rachaActual);
        usuarioRepository.save(usuarioLogueado);
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

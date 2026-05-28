package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.RutinaRequest;
import com.proyectoFinal.gymtracker.DTO.Response.DiaRutinaResponse;
import com.proyectoFinal.gymtracker.DTO.Response.EjercicioRutinaResponse;
import com.proyectoFinal.gymtracker.DTO.Response.RutinaResponse;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Exception.UserNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.*;
import com.proyectoFinal.gymtracker.Repositories.DiaRutinaRepository;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRepository;
import com.proyectoFinal.gymtracker.Repositories.RutinaRepository;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RutinaService {

    private final RutinaRepository rutinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EjercicioRepository ejercicioRepository;
    private final DiaRutinaRepository diaRutinaRepository;

    @Transactional
    public RutinaResponse createRutina(RutinaRequest rutinaRequest) {
        Usuario creador = usuarioRepository.findById(rutinaRequest.getCreadorId())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        validarPrecioYrol(rutinaRequest, creador);

        Rutina rutina = Rutina.builder()
                .creador(creador)
                .nombre(rutinaRequest.getNombre())
                .tokenCompartir(UUID.randomUUID().toString())
                .precio(rutinaRequest.getPrecio()).build();

        if (rutinaRequest.getDias() != null) {
            List<DiaRutina> diaRutinas = rutinaRequest.getDias().stream()
                    .map(diaDto -> {
                        DiaRutina dia = DiaRutina.builder() // Cada diaRequest se transforma en la entidad DiaRutina
                                .diaDeLaSemana(diaDto.getDiaDeLaSemana())
                                .rutina(rutina).build();

                        if (diaDto.getEjercicios() != null) {
                            // Por cada dia recorremos todos los ejercicios
                            List<EjercicioRutina> ejercicioRutinas = diaDto.getEjercicios()
                                    .stream().map(ejDto -> {
                                        Ejercicio ejBase = ejercicioRepository.findById(ejDto.getEjercicioId())
                                                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));
                                        return EjercicioRutina.builder()
                                                .dia(dia)
                                                .ejercicio(ejBase)
                                                .series(ejDto.getSeries())
                                                .repeticiones(ejDto.getRepeticiones())
                                                .build();
                                    }).toList();
                            dia.setEjercicios(ejercicioRutinas);
                        }
                        return dia;
                    }).toList();
            rutina.setDias(diaRutinas);
        }
        Rutina rutinaSaved = rutinaRepository.save(rutina);
        return mapRutinaResponse(rutinaSaved);
    }

    public RutinaResponse getRutinaById(Long idRutina) {
        Rutina rutinaSaved = rutinaRepository.findById(idRutina)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));
        return mapRutinaResponse(rutinaSaved);
    }

    public List<RutinaResponse> getAllRutinas(){
        return rutinaRepository.findAll().stream().map(this::mapRutinaResponse).toList();
    }

    //para ver la rutina de hoy
    public DiaRutinaResponse getDiaRutinaActual(Long idUsuario) {
        Usuario u = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        if (u.getRutinaActiva() == null) throw new BusinessLogicException("No tiene rutina activa");
        DayOfWeek hoy = LocalDate.now().getDayOfWeek();
        DiaRutina dia = diaRutinaRepository
                .findByRutinaIdAndDiaDeLaSemana(u.getRutinaActiva().getId(), hoy)
                .orElseThrow(() -> new BusinessLogicException("La rutina no tiene día configurado para hoy"));
        return mapToDiaRutinaResponse(dia);
    }
        
    public void deleteRutina(Long idRutina) {
        if (!rutinaRepository.existsById(idRutina)) {
            throw new ResourceNotFoundException("La rutina no existe");
        }

        try {
            rutinaRepository.deleteById(idRutina);

        } catch (DataIntegrityViolationException e) {
            throw new BusinessLogicException("No se puede eliminar la rutina porque actualmente está asignada a un usuario o existe en un historial de entrenamiento.");
        }
    }

    // === Metodos auxiliares ===

    private RutinaResponse mapRutinaResponse(Rutina rutina){
        return RutinaResponse.builder()
                .id(rutina.getId())
                .creadorId(rutina.getCreador() != null ? rutina.getCreador().getId() : null)
                .nombre(rutina.getNombre())
                .tokenCompartir(rutina.getTokenCompartir())
                .precio(rutina.getPrecio() > 0 ? rutina.getPrecio() : null)
                .diaRutinas(rutina.getDias() != null ? rutina.getDias().stream()
                        .map(this::mapToDiaRutinaResponse).toList() : List.of())
                .build();
    }

    private DiaRutinaResponse mapToDiaRutinaResponse(DiaRutina diaRutina){
        return DiaRutinaResponse.builder()
                .id(diaRutina.getId())
                .diaDeLaSemana(diaRutina.getDiaDeLaSemana())
                .ejercicioRutinas(diaRutina.getEjercicios() != null ? diaRutina.getEjercicios().stream()
                        .map(this::mapToEjercicioRutinaResponse).toList() : List.of())
                .build();
    }

    private EjercicioRutinaResponse mapToEjercicioRutinaResponse(EjercicioRutina ejercicioRutina){
        return EjercicioRutinaResponse.builder()
                .id(ejercicioRutina.getId())
                .ejercicioId(ejercicioRutina.getEjercicio().getId())
                .nombreEjercicio(ejercicioRutina.getEjercicio().getNombre())
                .series(ejercicioRutina.getSeries())
                .repeticiones(ejercicioRutina.getRepeticiones()).build();
    }

    private void validarPrecioYrol (RutinaRequest rutinaRequest, Usuario creador) {

        boolean tienePrecio = rutinaRequest.getPrecio() != null && rutinaRequest.getPrecio() > 0;
        boolean esEntrenador = creador.getRol().equals(Rol.ENTRENADOR);

        if (tienePrecio && !esEntrenador) {
            throw new BusinessLogicException("Solo los usuarios con rol ENTRENADOR pueden asignar un precio a las rutinas.");
        }
    }



}

package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.DiaRutinaRequest;
import com.proyectoFinal.gymtracker.DTO.Request.EjercicioRutinaRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
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
    public RutinaResponse createRutina(RutinaRequest rutinaRequest, Usuario creador) {
        validarLimiteRutinas(creador);
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

    @Transactional
    public RutinaResponse updateRutina(Usuario usuario, RutinaRequest rutinaRequest, Long idRutina){
        Rutina rutinaExistente = rutinaRepository.findById(idRutina)
                .orElseThrow(()-> new BusinessLogicException("Rutina no encontrada"));

        if(!rutinaExistente.getCreador().getId().equals(usuario.getId())) {
            throw new BusinessLogicException("No tienes permiso para editar esta rutina");
        }

        rutinaExistente.setNombre(rutinaRequest.getNombre());

        validarPrecioYrol(rutinaRequest,usuario);

        rutinaExistente.setPrecio(rutinaRequest.getPrecio());

        List<DiaRutina> diasActuales = rutinaExistente.getDias();
        List<DiaRutinaRequest> diasRequest =
                rutinaRequest.getDias() != null ? rutinaRequest.getDias() : new ArrayList<>();

        // Eliminar días que ya no están en la request
        diasActuales.removeIf(diaActual -> diasRequest.stream()
                .noneMatch(dReq -> dReq.getDiaDeLaSemana() == diaActual.getDiaDeLaSemana()));

        for (DiaRutinaRequest diaReq : diasRequest) {
            DiaRutina diaActual = diasActuales.stream()
                    .filter(d -> d.getDiaDeLaSemana() == diaReq.getDiaDeLaSemana())
                    .findFirst()
                    .orElse(null);

            if (diaActual == null) {
                // Es un día nuevo
                DiaRutina nuevoDia = DiaRutina.builder()
                        .diaDeLaSemana(diaReq.getDiaDeLaSemana())
                        .rutina(rutinaExistente)
                        .ejercicios(new ArrayList<>())
                        .build();

                if (diaReq.getEjercicios() != null) {
                    for (EjercicioRutinaRequest ejReq : diaReq.getEjercicios()) {
                        Ejercicio ej = ejercicioRepository.findById(ejReq.getEjercicioId()).orElseThrow();
                        nuevoDia.getEjercicios().add(EjercicioRutina.builder()
                                .dia(nuevoDia)
                                .ejercicio(ej)
                                .series(ejReq.getSeries())
                                .repeticiones(ejReq.getRepeticiones())
                                .build());
                    }
                }
                diasActuales.add(nuevoDia);
            } else {
                // El día ya existe, actualizar sus ejercicios
                List<EjercicioRutina> ejerciciosActuales = diaActual.getEjercicios();
                if (ejerciciosActuales == null) {
                    ejerciciosActuales = new ArrayList<>();
                    diaActual.setEjercicios(ejerciciosActuales);
                }

                List<EjercicioRutinaRequest> ejerciciosReq = diaReq.getEjercicios() != null
                        ? diaReq.getEjercicios() : new ArrayList<>();

                List<EjercicioRutinaRequest> unmatchedReqs = new ArrayList<>(ejerciciosReq);

                Iterator<EjercicioRutina> iterator = ejerciciosActuales.iterator();
                while (iterator.hasNext()) {
                    EjercicioRutina ejercicioActual = iterator.next();
                    EjercicioRutinaRequest matchingReq = null;
                    
                    for (EjercicioRutinaRequest req : unmatchedReqs) {
                        if (req.getEjercicioId().equals(ejercicioActual.getEjercicio().getId())) {
                            matchingReq = req;
                            break;
                        }
                    }

                    if (matchingReq != null) {
                        ejercicioActual.setSeries(matchingReq.getSeries());
                        ejercicioActual.setRepeticiones(matchingReq.getRepeticiones());
                        unmatchedReqs.remove(matchingReq);
                    } else {
                        iterator.remove();
                    }
                }

                for (EjercicioRutinaRequest newReq : unmatchedReqs) {
                    Ejercicio ej = ejercicioRepository.findById(newReq.getEjercicioId()).orElseThrow();
                    ejerciciosActuales.add(EjercicioRutina.builder()
                            .dia(diaActual)
                            .ejercicio(ej)
                            .series(newReq.getSeries())
                            .repeticiones(newReq.getRepeticiones())
                            .build());
                }
            }
        }

        try {
            Rutina rutinaSaved = rutinaRepository.save(rutinaExistente);
            return mapRutinaResponse(rutinaSaved);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessLogicException("No se pueden eliminar ejercicios que ya tienen un historial de entrenamiento registrado.");
        }
    }

    public RutinaResponse getRutinaById(Long idRutina) {
        Rutina rutinaSaved = rutinaRepository.findById(idRutina)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));
        return mapRutinaResponse(rutinaSaved);
    }

    public Page<RutinaResponse> getAllRutinas(Pageable pageable) {
        return rutinaRepository.findAll(pageable).map(this::mapRutinaResponse);
    }

    //para ver la rutina de hoy
    public DiaRutinaResponse getDiaRutinaActual(Usuario usuario) {

        if (usuario.getRutinaActiva() == null) throw new BusinessLogicException("No tiene rutina activa");
        DayOfWeek hoy = LocalDate.now().getDayOfWeek();
        DiaRutina dia = diaRutinaRepository
                .findByRutinaIdAndDiaDeLaSemana(usuario.getRutinaActiva().getId(), hoy)
                .orElseThrow(() -> new BusinessLogicException("La rutina no tiene día configurado para hoy"));
        return mapToDiaRutinaResponse(dia);
    }
        
    public void deleteRutina(Usuario usuario, Long idRutina) {

        Rutina rutina = rutinaRepository.findById(idRutina)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));

        if (!rutina.getCreador().getId().equals(usuario.getId())) {
            throw new BusinessLogicException("No sos el creador de la rutina para eliminarla");
        }

        if (usuario.getRutinaActiva() != null && usuario.getRutinaActiva().getId().equals(idRutina)) {
            usuario.setRutinaActiva(null);
            usuarioRepository.save(usuario);
        }

        try {
            rutinaRepository.delete(rutina);

        } catch (DataIntegrityViolationException e) {
            throw new BusinessLogicException("No se puede eliminar la rutina porque actualmente está asignada a un usuario o existe en un historial de entrenamiento.");
        }
    }

    public List<RutinaResponse> getRutinasMe(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        List<Rutina> misRutinas = rutinaRepository.findByCreador(usuario);
        Rutina rutinaActiva = usuario.getRutinaActiva();
        
        if (rutinaActiva != null) {
            boolean flag = misRutinas.stream()
                    .anyMatch(r -> r.getId().equals(rutinaActiva.getId()));

            if (!flag) misRutinas.add(rutinaActiva);
        }

        return misRutinas.stream().map(this::mapRutinaResponse).toList();
    }

    public List<RutinaResponse> getRutinaAlumno (Long idAlumno, Long idEntrenador){

        Usuario entrenador = usuarioRepository.findById(idEntrenador)
                .orElseThrow(() -> new UserNotFoundException("Entrenador no encontrado"));

        Usuario alumno = usuarioRepository.findById(idAlumno)
                .orElseThrow(() -> new UserNotFoundException("Alumno no encontrado"));

        if (!entrenador.getRol().equals(Rol.ENTRENADOR)) {
            throw new BusinessLogicException("Usted no es entrenador");
        }

        if (alumno.getEntrenador() == null || !alumno.getEntrenador().getId().equals(entrenador.getId())) {
            throw new BusinessLogicException("Este alumno no está a su cargo");
        }

        List<Rutina> rutinasDelAlumno = rutinaRepository.findByCreador(alumno);

        Rutina rutinaActiva = alumno.getRutinaActiva();
        if (rutinaActiva != null && !rutinasDelAlumno.contains(rutinaActiva)) {
            rutinasDelAlumno.add(rutinaActiva);
        }

        return rutinasDelAlumno.stream()
                .map(this::mapRutinaResponse)
                .toList();
    }

    // === Metodos auxiliares ===

    private RutinaResponse mapRutinaResponse(Rutina rutina){
        return RutinaResponse.builder()
                .id(rutina.getId())
                .creadorId(rutina.getCreador() != null ? rutina.getCreador().getId() : null)
                .nombre(rutina.getNombre())
                .tokenCompartir(rutina.getTokenCompartir())
                .precio(rutina.getPrecio() != null && rutina.getPrecio() > 0 ? rutina.getPrecio() : null)
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
    private void validarLimiteRutinas(Usuario creador) {
        boolean esUsuarioBasico = creador.getRol().equals(Rol.USUARIO);
        if (!esUsuarioBasico) return;

        int LIMITE_GRATUITO = 3;
        long cantidadActual = rutinaRepository.countByCreador(creador);

        if (cantidadActual >= LIMITE_GRATUITO) {
            throw new BusinessLogicException(
                    "Los usuarios gratuitos solo pueden crear hasta " + LIMITE_GRATUITO + " rutinas. Upgradea a Premium para crear rutinas ilimitadas."
            );
        }
    }



}

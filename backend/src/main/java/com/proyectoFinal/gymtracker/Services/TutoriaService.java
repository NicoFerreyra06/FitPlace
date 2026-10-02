package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Response.UsuarioResponse;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Exception.UserNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Rutina;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.RutinaRepository;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TutoriaService {

    private final UsuarioRepository usuarioRepository;
    private final RutinaRepository rutinaRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public UsuarioResponse asignarEntrenador(Long entrenadorId, Usuario usuario) {
        Usuario entrenador = usuarioRepository.findById(entrenadorId).orElseThrow(() -> new UserNotFoundException("Entrenador no encontrado"));

        if (!entrenador.getRol().equals(Rol.ENTRENADOR))
            throw new BusinessLogicException("El usuario seleccionado no es un entrenador");

        if (entrenadorId.equals(usuario.getId()))
            throw new BusinessLogicException("No se puede asignar a si mismo como entrenador");

        usuario.setEntrenador(entrenador);
        return usuarioService.toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse eliminarEntrenador(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        usuario.setEntrenador(null);
        return usuarioService.toResponse(usuarioRepository.save(usuario));
    }

    public List<UsuarioResponse> getAlumnos(Long entrenadorId) {
        return usuarioRepository.findByEntrenadorId(entrenadorId).stream().map(usuarioService::toResponse).toList();
    }

    public UsuarioResponse verEntrenadorActual(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (usuario.getEntrenador() == null) throw new BusinessLogicException("No tiene entrenador");

        return usuarioService.toResponse(usuario.getEntrenador());
    }

    @Transactional
    public UsuarioResponse asignarRutinaAAlumno(Usuario entrenador, Long idAlumno, Long idRutina) {
        Usuario alumno = usuarioRepository.findById(idAlumno).orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado"));

        if (alumno.getEntrenador() == null || !alumno.getEntrenador().getId().equals(entrenador.getId())) {
            throw new BusinessLogicException("No tienes permiso para asignarle rutinas a este alumno");
        }

        Rutina rutina = rutinaRepository.findById(idRutina).orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));

        alumno.setRutinaActiva(rutina);
        alumno.setRutinaActivaDesde(LocalDate.now());
        return usuarioService.toResponse(usuarioRepository.save(alumno));
    }

    @Transactional
    public UsuarioResponse eliminarAlumno(Usuario entrenador, Long idAlumno) {
        Usuario alumno = usuarioRepository.findById(idAlumno).orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado"));

        if (alumno.getEntrenador() == null || !alumno.getEntrenador().getId().equals(entrenador.getId())) {
            throw new BusinessLogicException("No tienes permiso para eliminar a este alumno porque no te pertenece");
        }

        alumno.setEntrenador(null);
        return usuarioService.toResponse(usuarioRepository.save(alumno));
    }
}

package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.Config.JwtService;
import com.proyectoFinal.gymtracker.DTO.Request.LoginRequest;
import com.proyectoFinal.gymtracker.DTO.Request.PerfilUpdateRequest;
import com.proyectoFinal.gymtracker.DTO.Request.UsuarioRequest;
import com.proyectoFinal.gymtracker.DTO.Response.AmigoResponse;
import com.proyectoFinal.gymtracker.DTO.Response.GananciaGimnasioProjection;
import com.proyectoFinal.gymtracker.DTO.Response.LoginResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final RutinaRepository rutinaRepository;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final com.proyectoFinal.gymtracker.Repositories.SuscripcionGimnasioRepository suscripcionRepository;

    public UsuarioResponse verPerfilPropio(Usuario usuario) {
        return toResponse(usuario);
    }

    public UsuarioResponse verPerfilOtroUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        return toResponse(usuario);
    }

    public UsuarioResponse editarPerfil(Usuario usuario, PerfilUpdateRequest request) {
        usuario.setPeso(request.getPeso());
        usuario.setAltura(request.getAltura());
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse activarRutina(Usuario usuario, Long idRutina) {
        Rutina rutina = rutinaRepository.findById(idRutina).orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));

        if (!rutina.isEsPublica()) {
            boolean esCreador = rutina.getCreador().getId().equals(usuario.getId());
            boolean esAlumnoDelCreador = usuario.getEntrenador() != null && usuario.getEntrenador().getId().equals(rutina.getCreador().getId());
            
            if (!esCreador && !esAlumnoDelCreador) {
                throw new BusinessLogicException("No tienes permiso para utilizar esta rutina privada");
            }
        }

        usuario.setRutinaActiva(rutina);
        usuario.setRutinaActivaDesde(LocalDate.now());
        return toResponse(usuarioRepository.save(usuario));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Page<UsuarioResponse> getUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::toResponse);
    }

    public List<UsuarioResponse> getEntrenadores() {
        return usuarioRepository.findByRol(Rol.ENTRENADOR).stream().map(this::toResponse).toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse cambiarRol(Long idUsuario, Rol nuevoRol) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        usuario.setRol(nuevoRol);
        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder().id(usuario.getId()).username(usuario.getUsername()).email(usuario.getEmail()).rol(usuario.getRol()).codigoAmigo(usuario.getCodigoAmigo()).peso(usuario.getPeso()).altura(usuario.getAltura()).imc(usuario.getImc()).categoriaImc(calcularCategoriaImc(usuario.getImc())).rachaActualDias(usuario.getRachaActualDias()).rachaMaximaDias(usuario.getRachaMaximaDias()).build();
    }

    private String calcularCategoriaImc(Double imc) {
        if (imc == null || imc <= 0) return null;
        if (imc < 18.5) return "Bajo peso";
        if (imc >= 18.5 && imc < 25.0) return "Peso Normal";
        if (imc >= 25.0 && imc < 30.0) return "Exceso de peso";
        if (imc >= 30.0 && imc < 35.0) return "Obesidad grado 1";
        if (imc >= 35.0 && imc < 40.0) return "Obesidad grado 2";
        return "Obesidad grado 3";
    }

    public AmigoResponse toAmigoResponse(Usuario usuario) {
        return AmigoResponse.builder().id(usuario.getId()).username(usuario.getUsername()).rol(usuario.getRol()).rachaActualDias(usuario.getRachaActualDias()).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<GananciaGimnasioProjection> obtenerGananciasPorGimnasio(LocalDate desde, LocalDate hasta) {
        return suscripcionRepository.reporteGananciasPorGimnasio(desde, hasta);
    }
}
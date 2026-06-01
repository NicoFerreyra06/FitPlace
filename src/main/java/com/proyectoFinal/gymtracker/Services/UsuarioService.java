package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.LoginRequest;
import com.proyectoFinal.gymtracker.DTO.Request.UsuarioRequest;
import com.proyectoFinal.gymtracker.DTO.Response.AmigoResponse;
import com.proyectoFinal.gymtracker.DTO.Response.UsuarioResponse;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Exception.UserNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Rutina;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.RutinaRepository;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import com.proyectoFinal.gymtracker.Enum.Rol;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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

    private UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .codigoAmigo(usuario.getCodigoAmigo())
                .peso(usuario.getPeso())
                .altura(usuario.getAltura())
                .imc(usuario.getImc())
                .categoriaImc(calcularCategoriaImc(usuario.getImc()))
                .rachaActualDias(usuario.getRachaActualDias())
                .rachaMaximaDias(usuario.getRachaMaximaDias()).build();
    }

    public UsuarioResponse registrar(UsuarioRequest usuarioRequest) {

        if (usuarioRequest.getEmail() == null || usuarioRequest.getEmail().isEmpty()) {
            throw new BusinessLogicException("Email obligatorio");
        }

        if (usuarioRepository.findByEmail(usuarioRequest.getEmail()).isPresent()) {
            throw new BusinessLogicException("Email ya registrado");
        }

        if (usuarioRepository.findByUsername(usuarioRequest.getUsername()).isPresent()) {
            throw new BusinessLogicException("Username ya existe");
        }

        Usuario usuario = Usuario.builder()
                .username(usuarioRequest.getUsername())
                .email(usuarioRequest.getEmail())
                .password(passwordEncoder.encode(usuarioRequest.getPassword()))
                .peso(usuarioRequest.getPeso())
                .altura(usuarioRequest.getAltura())
                .rol(Rol.USUARIO)
                .codigoAmigo(UUID.randomUUID().toString())
                .build();

        Usuario saved = usuarioRepository.save(usuario);

        return toResponse(saved);
    }

    public UsuarioResponse login(LoginRequest loginRequest) {

        Usuario usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), usuario.getPassword())) {
            throw new BusinessLogicException("Contrasena incorrecta");
        }
        return toResponse(usuario);
    }

    public UsuarioResponse getById(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        return toResponse(usuario);
    }

    public UsuarioResponse editarPerfil(Long idUsuario, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        usuario.setPeso(request.getPeso());
        usuario.setAltura(request.getAltura());

        return toResponse(usuarioRepository.save(usuario));

    }

    @Transactional
    public UsuarioResponse activarRutina(Long idUsuario, Long idRutina) {
        Usuario u = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        Rutina r = rutinaRepository.findById(idRutina)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada"));
        u.setRutinaActiva(r);
        u.setRutinaActivaDesde(LocalDate.now());
        return toResponse(usuarioRepository.save(u));
    }

    private String calcularCategoriaImc(Double imc) {
        if (imc == null || imc == 0) return null;
        if (imc > 1.0  && imc < 18.5) return "bajo peso";
        if (imc >= 18.5 && imc < 25.0) return "normal";
        if (imc >= 25.0 && imc < 30.0) return "sobrepeso";
        else return "obesidad";
    }

    @Transactional
    public UsuarioResponse agregarAmigo(Long idUsuario, String codigoAmigo) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        Usuario amigo = usuarioRepository.findByCodigoAmigo(codigoAmigo)
                .orElseThrow(() -> new ResourceNotFoundException("No existe usuario con ese código"));

        if (usuario.getId().equals(amigo.getId()))
            throw new BusinessLogicException("No podés agregarte a vos mismo");
        if (usuario.getAmigos().contains(amigo))
            throw new BusinessLogicException("Ya son amigos");

        usuario.getAmigos().add(amigo);
        return toResponse(usuarioRepository.save(usuario));
    }

    public List<AmigoResponse> getAmigos(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        return usuario.getAmigos().stream().map(this::toAmigoResponse).toList();
    }

    private AmigoResponse toAmigoResponse(Usuario usuario) {
        return AmigoResponse.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .rol(usuario.getRol())
                .rachaActualDias(usuario.getRachaActualDias())
                .build();
    }

}
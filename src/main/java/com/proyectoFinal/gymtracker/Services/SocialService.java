package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Response.AmigoResponse;
import com.proyectoFinal.gymtracker.DTO.Response.UsuarioResponse;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Exception.UserNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public UsuarioResponse agregarAmigo(Usuario usuarioLog, String codigoAmigo) {
        Usuario usuario = usuarioRepository.findById(usuarioLog.getId()).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        Usuario amigo = usuarioRepository.findByCodigoAmigo(codigoAmigo).orElseThrow(() -> new ResourceNotFoundException("No existe usuario con ese código"));

        if (usuario.getId().equals(amigo.getId()))
            throw new BusinessLogicException("No podés agregarte a vos mismo como amigo");
        if (usuario.getAmigos().contains(amigo)) throw new BusinessLogicException("Ya son amigos");

        usuario.getAmigos().add(amigo);
        amigo.getAmigos().add(usuario);
        usuarioRepository.save(amigo);

        return usuarioService.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse eliminarAmigo(Long amigoId, Usuario usuarioLog) {
        Usuario usuario = usuarioRepository.findById(usuarioLog.getId()).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));

        usuario.getAmigos().removeIf(amigo -> amigo.getId().equals(amigoId));

        Usuario amigoToRemove = usuarioRepository.findById(amigoId).orElse(null);
        if (amigoToRemove != null) {
            amigoToRemove.getAmigos().removeIf(u -> u.getId().equals(usuario.getId()));
            usuarioRepository.save(amigoToRemove);
        }

        return usuarioService.toResponse(usuarioRepository.save(usuario));
    }

    public List<AmigoResponse> getAmigos(Usuario usuarioLog) {
        Usuario usuario = usuarioRepository.findById(usuarioLog.getId()).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        return usuarioRepository.findAllFriendByUserId(usuario.getId())
                .stream().map(usuarioService::toAmigoResponse).toList();
    }

    public UsuarioResponse verPerfilAmigo(Usuario usuarioLog, Long amigoId) {
        Usuario usuario = usuarioRepository.findById(usuarioLog.getId()).orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        Usuario amigo = usuarioRepository.findById(amigoId).orElseThrow(() -> new UserNotFoundException("Amigo no encontrado"));

        if (!usuario.getAmigos().contains(amigo)) {
            throw new BusinessLogicException("No tenés acceso al perfil de este usuario");
        }

        return usuarioService.toResponse(amigo);
    }
}

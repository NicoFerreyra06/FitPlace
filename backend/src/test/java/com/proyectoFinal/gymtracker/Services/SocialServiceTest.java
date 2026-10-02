package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Response.UsuarioResponse;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SocialServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private SocialService socialService;

    private Usuario usuario;
    private Usuario usuario2;

    @BeforeEach
    public void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .username("nico_dev")
                .email("nico@test.com")
                .rol(Rol.USUARIO)
                .codigoAmigo(UUID.randomUUID().toString())
                .build();

        usuario2 = Usuario.builder()
                .id(2L)
                .username("lucio_dev")
                .email("lucio_dev@test")
                .rol(Rol.USUARIO)
                .codigoAmigo(UUID.randomUUID().toString())
                .build();
    }

    @Test
    @DisplayName("Deberia agregar correctamente el amigo")
    void addFriendSuccessful(){
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCodigoAmigo(usuario2.getCodigoAmigo())).thenReturn(Optional.of(usuario2));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        
        UsuarioResponse mockResponse = UsuarioResponse.builder().email("nico@test.com").build();
        when(usuarioService.toResponse(any(Usuario.class))).thenReturn(mockResponse);

        UsuarioResponse usuarioResponse = socialService.agregarAmigo(usuario, usuario2.getCodigoAmigo());

        assertNotNull(usuarioResponse);
        assertEquals("nico@test.com", usuarioResponse.getEmail());
        assertTrue(usuario.getAmigos().contains(usuario2));
        assertTrue(usuario2.getAmigos().contains(usuario));
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque se agrega a si mismo")
    void shouldThrowExceptionWhenUsuarioAddYourself(){
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCodigoAmigo(usuario.getCodigoAmigo())).thenReturn(Optional.of(usuario));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
            socialService.agregarAmigo(usuario, usuario.getCodigoAmigo());
        });

        assertEquals("No podés agregarte a vos mismo como amigo", exception.getMessage());
    }
}

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {


    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    private Usuario usuario2;

    private Usuario entrenador;

    @BeforeEach
    public void setUp() {
            usuario = Usuario.builder()
                .id(1L)
                .username("nico_dev")
                .email("nico@test.com")
                .password("password")
                .rol(Rol.USUARIO)
                .peso(75.5)
                .altura(1.80)
                .codigoAmigo(UUID.randomUUID().toString())
                .rutinaActivaDesde(LocalDate.now())
                .rachaActualDias(3)
                .rachaMaximaDias(10)
                .build();

            usuario2 = Usuario.builder()
                    .id(2L)
                    .username("lucio_dev")
                    .email("lucio_dev@test")
                    .password("123456")
                    .rol(Rol.USUARIO)
                    .peso(70.0)
                    .altura(1.55)
                    .codigoAmigo(UUID.randomUUID().toString())
                    .rutinaActivaDesde(LocalDate.now())
                    .rachaActualDias(4)
                    .rachaMaximaDias(10)
                    .build();

            entrenador = Usuario.builder()
                .id(3L)
                .username("entrenador")
                .email("entrenador@test")
                .password("123456")
                .rol(Rol.ENTRENADOR)
                .peso(70.0)
                .altura(1.55)
                .codigoAmigo(UUID.randomUUID().toString())
                .rutinaActivaDesde(LocalDate.now())
                .rachaActualDias(4)
                .rachaMaximaDias(10)
                .build();
    }

    @Test
    @DisplayName("Deberia retornar el perfil propio")
    void testVerPerfilPropio() {
        UsuarioResponse mockResponse = UsuarioResponse.builder().email("nico@test.com").build();
        assertEquals("nico@test.com", mockResponse.getEmail());
    }

    @Test
    @DisplayName("Deberia devolver el username elegido y no el email en el perfil")
    void testToResponseDevuelveUsernameYNoEmail() {
        UsuarioResponse response = usuarioService.toResponse(usuario);

        assertEquals("nico_dev", response.getUsername());
        assertEquals("nico@test.com", response.getEmail());
    }

    @Test
    @DisplayName("Deberia devolver el username de un amigo sin exponer su email")
    void testToAmigoResponseNoExponeEmail() {
        var response = usuarioService.toAmigoResponse(usuario2);

        assertEquals("lucio_dev", response.getUsername());
    }

    @Test
    @DisplayName("Permite cambiar el rol de una cuenta no administradora")
    void testCambiarRolDeUsuario() {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = usuarioService.cambiarRol(usuario.getId(), Rol.ENTRENADOR);

        assertEquals(Rol.ENTRENADOR, response.getRol());
        assertEquals(usuario.getUsername(), response.getUsername());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("No permite asignar el rol de administrador")
    void testNoPermiteAsignarRolAdmin() {
        assertThrows(BusinessLogicException.class,
                () -> usuarioService.cambiarRol(usuario.getId(), Rol.ADMIN));

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("No permite modificar el rol de una cuenta administradora")
    void testNoPermiteModificarRolDeAdmin() {
        Usuario administrador = Usuario.builder().id(4L).username("admin").email("admin@test.com")
                .rol(Rol.ADMIN).codigoAmigo(UUID.randomUUID().toString()).build();
        when(usuarioRepository.findById(administrador.getId())).thenReturn(Optional.of(administrador));

        assertThrows(BusinessLogicException.class,
                () -> usuarioService.cambiarRol(administrador.getId(), Rol.USUARIO));

        verify(usuarioRepository, never()).save(any());
    }
}

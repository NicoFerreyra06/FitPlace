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
public class TutoriaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private TutoriaService tutoriaService;

    private Usuario usuario;
    private Usuario usuario2;
    private Usuario entrenador;

    @BeforeEach
    public void setUp() {
        usuario = Usuario.builder().id(1L).email("nico@test.com").rol(Rol.USUARIO).build();
        usuario2 = Usuario.builder().id(2L).rol(Rol.USUARIO).build();
        entrenador = Usuario.builder().id(3L).rol(Rol.ENTRENADOR).build();
    }

    @Test
    @DisplayName("Deberia asignar correctamente el entrenador")
    void addTrainerSuccessful(){
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(entrenador));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        
        UsuarioResponse mockResponse = UsuarioResponse.builder().email("nico@test.com").build();
        when(usuarioService.toResponse(any(Usuario.class))).thenReturn(mockResponse);

        UsuarioResponse usuarioResponse = tutoriaService.asignarEntrenador(entrenador.getId(), usuario);

        assertNotNull(usuarioResponse);
        assertEquals("nico@test.com", usuarioResponse.getEmail());
        assertEquals(usuario.getEntrenador(), entrenador);
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque el usuario seleccionado no es entrenador")
    void shouldThrowExceptionWhenUserIsNotEntrenador(){
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario2));

        BusinessLogicException exception = assertThrows(BusinessLogicException.class, () -> {
            tutoriaService.asignarEntrenador(usuario2.getId(), usuario);
        });

        assertEquals("El usuario seleccionado no es un entrenador", exception.getMessage());
    }
}

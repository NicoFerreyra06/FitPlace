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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

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
    @DisplayName("Deberia agregar correctamente el amigo")
    void addFriendSuccessful(){
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCodigoAmigo(usuario2.getCodigoAmigo())).thenReturn(Optional.of(usuario2));

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioResponse usuarioResponse = usuarioService.agregarAmigo(usuario, usuario2.getCodigoAmigo());

        assertNotNull(usuarioResponse);
        assertEquals("nico@test.com", usuarioResponse.getEmail());

        assertTrue(usuario.getAmigos().contains(usuario2));
        assertTrue(usuario2.getAmigos().contains(usuario));
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque se agrega a si mismo")
    void shouldThrowExceptionWhenUsuarioAddYourself(){

        String codigoAmigo = UUID.randomUUID().toString();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCodigoAmigo(codigoAmigo)).thenReturn(Optional.of(usuario));

        BusinessLogicException businessLogicException = assertThrows(BusinessLogicException.class, () -> {
            usuarioService.agregarAmigo(usuario, codigoAmigo);
        });

        assertEquals("No podés agregarte a vos mismo como amigo", businessLogicException.getMessage());
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque ya son amigos")
    void shouldThrowExceptionWhenTheyAlreadyFriends(){
        usuario.getAmigos().add(usuario2);
        usuario2.getAmigos().add(usuario);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByCodigoAmigo(usuario2.getCodigoAmigo())).thenReturn(Optional.of(usuario2));

        BusinessLogicException businessLogicException = assertThrows(BusinessLogicException.class, () -> {
            usuarioService.agregarAmigo(usuario, usuario2.getCodigoAmigo());
        });

        assertEquals("Ya son amigos", businessLogicException.getMessage());
    }

    @Test
    @DisplayName("Deberia asignar correctamente el entrenador")
    void addTrainerSuccessful(){
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(entrenador));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioResponse usuarioResponse = usuarioService.asignarEntrenador(entrenador.getId(), usuario);

        assertNotNull(usuarioResponse);
        assertEquals("nico@test.com", usuarioResponse.getEmail());

        assertEquals(usuario.getEntrenador(), entrenador);
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque el usuario seleccionado no es entrenador")
    void shouldThrowExceptionWhenUserIsNotEntrenador(){
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario2));

        BusinessLogicException businessLogicException = assertThrows(BusinessLogicException.class, () -> {
            usuarioService.asignarEntrenador(usuario2.getId(), usuario);
        });

        assertEquals("El usuario seleccionado no es un entrenador", businessLogicException.getMessage());
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque intenta asignarse a si mismo como entrenador")
    void shouldThrowExceptionWhenTrainerAssignHimself(){
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(entrenador));

        BusinessLogicException businessLogicException = assertThrows(BusinessLogicException.class, () -> {
            usuarioService.asignarEntrenador(entrenador.getId(), entrenador);
        });

        assertEquals("No se puede asignar a si mismo como entrenador", businessLogicException.getMessage());
    }
}

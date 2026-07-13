package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.RutinaRequest;
import com.proyectoFinal.gymtracker.DTO.Response.RutinaResponse;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Rutina;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRepository;
import com.proyectoFinal.gymtracker.Repositories.RutinaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class RutinaServiceTest {

    @Mock
    private EjercicioRepository ejercicioRepository;

    @Mock
    private RutinaRepository rutinaRepository;

    @InjectMocks
    private RutinaService rutinaService;

    private Usuario usuario;

    private RutinaRequest rutinaRequest;

    @BeforeEach
    void setUp() {
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

        rutinaRequest = RutinaRequest.builder()
                .nombre("Rutina")
                .precio(0.0)
                .dias(List.of())
                .build();
    }

    @Test
    @DisplayName("Deberia crear rutina correctamente")
    void shouldCreateRutineSuccessful(){
        when(rutinaRepository.countByCreador(usuario)).thenReturn(0L);

        Rutina rutinaSimulada = Rutina.builder()
                .id(10L)
                .creador(usuario)
                .nombre(rutinaRequest.getNombre())
                .precio(rutinaRequest.getPrecio())
                .build();

        when(rutinaRepository.save(any(Rutina.class))).thenReturn(rutinaSimulada);

        RutinaResponse rutinaResponse = rutinaService.createRutina(rutinaRequest, usuario);

        assertNotNull(rutinaResponse);
        assertEquals(rutinaResponse.getId(), rutinaSimulada.getId());
        assertEquals(rutinaResponse.getNombre(), rutinaSimulada.getNombre());
        verify(rutinaRepository, times(1)).save(any(Rutina.class));
    }

    @Test
    @DisplayName("Deberia borrar rutina correctamente")
    void deleteRutinaSuccessful(){
        Long rutinaId = 10L;

        Rutina rutinaExistente = Rutina.builder()
                .id(rutinaId)
                .nombre("Rutina a Borrar")
                .creador(usuario)
                .build();

        when(rutinaRepository.findById(rutinaId)).thenReturn(Optional.of(rutinaExistente));

        rutinaService.deleteRutina(usuario, rutinaId);

        verify(rutinaRepository, times((1))).delete(rutinaExistente);
    }

    @Test
    @DisplayName("Deberia fallar porque la rutina no existe")
    void shouldThrowExceptionRutinaNotFound(){
        Long rutinaId = 99L;

        when(rutinaRepository.findById(rutinaId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> rutinaService.deleteRutina(usuario, rutinaId));

        verify(rutinaRepository, never()).save(any(Rutina.class));
    }
}

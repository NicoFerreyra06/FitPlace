package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.EjercicioRequest;
import com.proyectoFinal.gymtracker.DTO.Response.EjercicioResponse;
import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Ejercicio;
import com.proyectoFinal.gymtracker.Modelo.Musculo;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRepository;
import com.proyectoFinal.gymtracker.Repositories.MusculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EjercicioServiceTest {

    @Mock
    private MusculoRepository musculoRepository;

    @Mock
    private EjercicioRepository ejercicioRepository;

    @InjectMocks
    private EjercicioService ejercicioService;

    private Ejercicio ejercicio;

    private EjercicioRequest ejercicioRequest;

    private Musculo musculo;

    @BeforeEach
    public void setUp() {
        ejercicioRequest = EjercicioRequest
                .builder()
                .nombre("ejercicio")
                .descripcion("Test")
                .musculoPrincipalId(List.of(1L))
                .musculoSecundarioId(List.of(1L))
                .build();

        musculo = Musculo.builder()
                .id(1L)
                .nombre("musculo")
                .grupoMuscular(GrupoMuscular.CUADRICEPS)
                .build();

        ejercicio = Ejercicio.builder()
                .id(1L)
                .nombre("ejercicio")
                .descripcion("Test")
                .musculosPrincipales(List.of(musculo))
                .musculosSecundarios(List.of(musculo)).build();


    }

    @Test
    @DisplayName("Deberia crear el ejercicio correctamente")
    void shouldCreateEjercicioSuccessful(){

        when(musculoRepository.findAllById(List.of(musculo.getId()))).thenReturn(List.of(musculo));
        when(ejercicioRepository.save(any(Ejercicio.class))).thenReturn(ejercicio);

        EjercicioResponse ejercicioResponse = ejercicioService.addEjercicio(ejercicioRequest);

        assertNotNull(ejercicioResponse);
        assertEquals(ejercicio.getNombre(), ejercicioResponse.getNombre());
        verify(ejercicioRepository, times(1)).save(any(Ejercicio.class));
    }

    @Test
    @DisplayName("Deberia traer el ejercicio correctamente")
    void shouldGetEjercicioSuccessful(){
        when(ejercicioRepository.findById(1L)).thenReturn(Optional.of(ejercicio));

        EjercicioResponse ejercicioResponse = ejercicioService.getById(ejercicio.getId());

        verify(ejercicioRepository, times(1)).findById(1L);
        assertEquals(ejercicioResponse.getNombre(), ejercicio.getNombre());
        assertEquals(ejercicioResponse.getId(),ejercicio.getId());
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque el ejercicio no existe")
    void shouldThrowExceptionWhenEjercicioNotFound(){
        when(ejercicioRepository.findById(1L)).thenReturn(Optional.empty());

       assertThrows(ResourceNotFoundException.class,
               () -> ejercicioService.getById(1L));

       verify(ejercicioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deberia actualizar correctamente")
    void shouldUpdateEjercicioSuccessful(){

        ejercicioRequest.setNombre("updated");
        when(ejercicioRepository.findById(1L)).thenReturn(Optional.of(ejercicio));
        when(musculoRepository.findAllById(ejercicioRequest.getMusculoPrincipalId())).thenReturn(List.of(musculo));
        when(ejercicioRepository.save(any(Ejercicio.class))).thenReturn(ejercicio);

        EjercicioResponse ejercicioResponse = ejercicioService.updateEjercicio(ejercicioRequest, 1L);

        assertEquals(ejercicioResponse.getNombre(), ejercicio.getNombre());
        assertEquals(ejercicioResponse.getId(),ejercicio.getId());
        assertEquals("updated", ejercicioResponse.getNombre());
        verify(ejercicioRepository, times(1)).findById(1L);
        verify(ejercicioRepository, times(1)).save(any(Ejercicio.class));
    }

    @Test
    @DisplayName("Deberia eliminar correctamente")
    void shouldDeleteEjercicioSuccessful(){
        when(ejercicioRepository.findById(1L)).thenReturn(Optional.of(ejercicio));

        ejercicioService.deleteEjercicio(1L);

        verify(ejercicioRepository, times(1)).findById(1L);
        verify(ejercicioRepository, times(1)).delete(ejercicio);
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque no existe el ejercicio")
    void shouldThrowWhenEjercicioNotFound(){
        when(ejercicioRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> ejercicioService.deleteEjercicio(1L));

        verify(ejercicioRepository, times(1)).findById(1L);
        verify(ejercicioRepository, never()).delete(any(Ejercicio.class));
        assertEquals("Ejercicio no encontrado", exception.getMessage());
    }
}

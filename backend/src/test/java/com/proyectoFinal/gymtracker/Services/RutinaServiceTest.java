package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.DiaRutinaRequest;
import com.proyectoFinal.gymtracker.DTO.Request.RutinaRequest;
import com.proyectoFinal.gymtracker.DTO.Response.RutinaResponse;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.ForbiddenAccessException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.DiaRutina;
import com.proyectoFinal.gymtracker.Modelo.Rutina;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRepository;
import com.proyectoFinal.gymtracker.Repositories.RutinaRepository;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
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

    @Mock
    private UsuarioRepository usuarioRepository;

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
                .esPublica(true)
                .dias(List.of())
                .build();
    }

    @Test
    @DisplayName("Deberia crear rutina correctamente")
    void shouldCreateRutineSuccessful(){

        Rutina rutinaSimulada = Rutina.builder()
                .id(10L)
                .creador(usuario)
                .nombre(rutinaRequest.getNombre())
                .esPublica(rutinaRequest.getEsPublica())
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

        when(rutinaRepository.findByIdAndActivaTrue(rutinaId)).thenReturn(Optional.of(rutinaExistente));

        rutinaService.deleteRutina(usuario, rutinaId);

        verify(rutinaRepository, times((1))).delete(rutinaExistente);
        verify(usuarioRepository, times(1)).quitarRutinaActiva(rutinaId);
    }

    @Test
    @DisplayName("Deberia fallar al borrar porque la rutina no existe")
    void shouldThrowExceptionRutinaNotFound(){
        Long rutinaId = 99L;

        when(rutinaRepository.findByIdAndActivaTrue(rutinaId)).thenReturn(Optional.empty());

        var response = assertThrows(ResourceNotFoundException.class,
                () -> rutinaService.deleteRutina(usuario, rutinaId));

        verify(rutinaRepository, never()).delete(any(Rutina.class));
        assertEquals("Rutina no encontrada", response.getMessage());
    }

    @Test
    @DisplayName("Deberia fallar al borrar porque no le pertenece la rutina")
    void shouldThrowExceptionWhenUserIsNotOwner(){
        Long rutinaId = 10L;

        Usuario usuario2 = Usuario.builder()
                .id(2L)
                .username("test2")
                .email("test@gmail")
                .password("123")
                .rol(Rol.USUARIO)
                .build();

        Rutina rutinaExistente = Rutina.builder()
                .id(rutinaId)
                .nombre("Rutina a Borrar")
                .creador(usuario)
                .build();

        when(rutinaRepository.findByIdAndActivaTrue(rutinaId)).thenReturn(Optional.of(rutinaExistente));

        var response = assertThrows(ForbiddenAccessException.class,
                () -> rutinaService.deleteRutina(usuario2, rutinaId));

        assertNotNull(response);
        verify(rutinaRepository, never()).delete(any(Rutina.class));
        assertEquals("No tenés permiso para eliminar esta rutina", response.getMessage());
    }

    private DiaRutinaRequest diaRequest(DayOfWeek dia, String nombre) {
        DiaRutinaRequest request = new DiaRutinaRequest();
        request.setDiaDeLaSemana(dia);
        request.setNombre(nombre);
        request.setEjercicios(new ArrayList<>());
        return request;
    }

    @Test
    @DisplayName("Deberia guardar el nombre del dia sin espacios y dejarlo en null si viene vacio")
    void shouldNormalizeNombreDiaOnCreate() {
        rutinaRequest.setDias(List.of(
                diaRequest(DayOfWeek.MONDAY, "  Push A  "),
                diaRequest(DayOfWeek.WEDNESDAY, "   ")));
        when(rutinaRepository.save(any(Rutina.class))).thenAnswer(inv -> inv.getArgument(0));

        RutinaResponse response = rutinaService.createRutina(rutinaRequest, usuario);

        assertEquals("Push A", response.getDiaRutinas().get(0).getNombre());
        assertNull(response.getDiaRutinas().get(1).getNombre());
    }

    @Test
    @DisplayName("Deberia actualizar el nombre de un dia que ya existia al editar la rutina")
    void shouldUpdateNombreDiaExistente() {
        Rutina rutinaExistente = Rutina.builder()
                .id(10L)
                .nombre("Fuerza 4 dias")
                .creador(usuario)
                .build();
        DiaRutina lunes = DiaRutina.builder()
                .id(100L)
                .diaDeLaSemana(DayOfWeek.MONDAY)
                .nombre("Pecho")
                .rutina(rutinaExistente)
                .ejercicios(new ArrayList<>())
                .build();
        rutinaExistente.setDias(new ArrayList<>(List.of(lunes)));

        rutinaRequest.setDias(List.of(diaRequest(DayOfWeek.MONDAY, "Push A")));
        when(rutinaRepository.findByIdAndActivaTrue(10L)).thenReturn(Optional.of(rutinaExistente));
        when(rutinaRepository.save(any(Rutina.class))).thenAnswer(inv -> inv.getArgument(0));

        RutinaResponse response = rutinaService.updateRutina(usuario, rutinaRequest, 10L);

        assertEquals("Push A", response.getDiaRutinas().get(0).getNombre());
        assertEquals(100L, response.getDiaRutinas().get(0).getId());
    }

    @Test
    @DisplayName("Deberia borrar el nombre de un dia existente si al editar viene vacio")
    void shouldClearNombreDiaWhenBlank() {
        Rutina rutinaExistente = Rutina.builder()
                .id(10L)
                .nombre("Fuerza 4 dias")
                .creador(usuario)
                .build();
        DiaRutina lunes = DiaRutina.builder()
                .id(100L)
                .diaDeLaSemana(DayOfWeek.MONDAY)
                .nombre("Pecho")
                .rutina(rutinaExistente)
                .ejercicios(new ArrayList<>())
                .build();
        rutinaExistente.setDias(new ArrayList<>(List.of(lunes)));

        rutinaRequest.setDias(List.of(diaRequest(DayOfWeek.MONDAY, "")));
        when(rutinaRepository.findByIdAndActivaTrue(10L)).thenReturn(Optional.of(rutinaExistente));
        when(rutinaRepository.save(any(Rutina.class))).thenAnswer(inv -> inv.getArgument(0));

        RutinaResponse response = rutinaService.updateRutina(usuario, rutinaRequest, 10L);

        assertNull(response.getDiaRutinas().get(0).getNombre());
    }
}

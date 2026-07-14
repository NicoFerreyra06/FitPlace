package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.EntrenamientoLogRequest;
import com.proyectoFinal.gymtracker.DTO.Request.MarcaEjercicioRequest;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.*;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRutinaRepository;
import com.proyectoFinal.gymtracker.Repositories.EntrenamientoLogRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntrenamientoLogServiceTest {

    @Mock
    private RutinaRepository rutinaRepository;

    @Mock
    private EjercicioRutinaRepository ejercicioRutinaRepository;

    @Mock
    private EntrenamientoLogRepository entrenamientoLogRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecordPersonalService recordPersonalService;

    @InjectMocks
    private EntrenamientoLogService entrenamientoLogService;

    private EntrenamientoLogRequest entrenamientoLogRequest;

    private Usuario usuario;

    private Rutina rutina;

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

        MarcaEjercicioRequest marcaEjercicioRequest = MarcaEjercicioRequest.builder()
                .pesoLevantado(20.0)
                .repeticionesLogradas(5)
                .ejercicioRutinaId(1L).build();

        entrenamientoLogRequest = EntrenamientoLogRequest.builder()
                .idRutina(1L)
                .marcasEjercicio(List.of(marcaEjercicioRequest)).build();

        rutina = Rutina.builder()
                .id(1L)
                .creador(usuario)
                .nombre("Test_rutina")
                .tokenCompartir(UUID.randomUUID().toString())
                .precio(0.0)
                .dias(List.of()).build();
    }

    @Test
    @DisplayName("Deberia crear correctamente un EntrenamientoLog")
    void shouldCreateEntrenamientoLog() {
        usuario.setRachaActualDias(0);

        EjercicioRutina ejercicioRutina = crearEjercicioRutinaSimulado();

        EntrenamientoLog logSimulado = EntrenamientoLog.builder()
                .id(99L)
                .usuario(usuario)
                .rutinaEjecutada(rutina)
                .build();

        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(rutinaRepository.findById(1L)).thenReturn(Optional.of(rutina));
        when(ejercicioRutinaRepository.findById(1L)).thenReturn(Optional.of(ejercicioRutina));
        when(entrenamientoLogRepository.save(any(EntrenamientoLog.class))).thenReturn(logSimulado);

        var response = entrenamientoLogService.addEntrenamientoLog(entrenamientoLogRequest,  usuario);

        assertNotNull(response);
        verify(rutinaRepository, times(1)).findById(1L);
        verify(ejercicioRutinaRepository, times(1)).findById(1L);
        verify(recordPersonalService, times(1)).actualizarRecordSiCorresponde(eq(usuario), any(), anyDouble(), any());
        verify(entrenamientoLogRepository, times(1)).save(any(EntrenamientoLog.class));
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque no encuentra la rutina")
    void shouldThrowExceptionWhenRutineNotFound() {
        when(rutinaRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> entrenamientoLogService.addEntrenamientoLog(entrenamientoLogRequest,  usuario));

        assertNotNull(exception);
        assertEquals("Rutina no encontrada", exception.getMessage());
        verify(rutinaRepository, times(1)).findById(1L);
        verify(entrenamientoLogRepository, never()).save(any(EntrenamientoLog.class));
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque no encuentra el ejercicio")
    void shouldThrowExceptionWhenEjercicioNotFound() {
        usuario.setRachaActualDias(0);
        when(rutinaRepository.findById(1L)).thenReturn(Optional.of(rutina));
        when(ejercicioRutinaRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> entrenamientoLogService.addEntrenamientoLog(entrenamientoLogRequest,  usuario));

        assertNotNull(exception);
        assertEquals("Ejercicio no encontrado", exception.getMessage());
        verify(rutinaRepository, times(1)).findById(1L);
        verify(ejercicioRutinaRepository, times(1)).findById(1L);
        verify(entrenamientoLogRepository, never()).save(any(EntrenamientoLog.class));
    }

    @Test
    @DisplayName("Deberia actualizar correctamente")
    void shouldUpdateSuccessfullyEntrenamientoLog() {
        EntrenamientoLog entrenamientoLogViejo = EntrenamientoLog.builder()
                .id(1L)
                .usuario(usuario)
                .rutinaEjecutada(rutina)
                .fecha(LocalDate.now())
                .build();

        EntrenamientoLog entrenamientoLogUpdate = EntrenamientoLog.builder()
                .id(2L)
                .usuario(usuario)
                .rutinaEjecutada(rutina)
                .fecha(LocalDate.now())
                .build();

        EjercicioRutina ejercicioRutina = crearEjercicioRutinaSimulado();

        when(entrenamientoLogRepository.findById(1L)).thenReturn(Optional.of(entrenamientoLogViejo));
        when(ejercicioRutinaRepository.findById(1L)).thenReturn(Optional.of(ejercicioRutina));
        when(rutinaRepository.findById(1L)).thenReturn(Optional.of(rutina));
        when(entrenamientoLogRepository.save(any(EntrenamientoLog.class))).thenReturn(entrenamientoLogUpdate);

        var response = entrenamientoLogService.updateEntrenamiento(entrenamientoLogRequest, 1L, usuario);

        assertNotNull(response);
        verify(ejercicioRutinaRepository, times(1)).findById(1L);
        verify(entrenamientoLogRepository, times(1)).findById(1L);
        verify(rutinaRepository, times(1)).findById(1L);
        verify(entrenamientoLogRepository, times(1)).save(any(EntrenamientoLog.class));
        assertEquals("Test_rutina", response.getNombreRutina());
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque no encuentra el entrenamiento")
    void shouldThrowExceptionWhenEntrenamientoLogNotFound() {
        when(entrenamientoLogRepository.findById(1L)).thenReturn(Optional.empty());

        var response = assertThrows(ResourceNotFoundException.class,
                ()-> entrenamientoLogService.updateEntrenamiento(entrenamientoLogRequest,  1L, usuario));

        assertNotNull(response);
        assertEquals("Entrenamiento no encontrado", response.getMessage());
        verify(entrenamientoLogRepository, never()).save(any(EntrenamientoLog.class));
    }

    @Test
    @DisplayName("Deberia lanzar excepcion porque el log no le pertenece")
    void shouldThrowExceptionWhenUserItsDontCreator(){
        Usuario infiltrado = Usuario.builder()
                .id(2L)
                .username("infiltrado_dev")
                .email("infiltrado@test.com")
                .password("password")
                .rol(Rol.USUARIO)
                .peso(75.5)
                .altura(1.80)
                .codigoAmigo(UUID.randomUUID().toString())
                .rutinaActivaDesde(LocalDate.now())
                .rachaActualDias(3)
                .rachaMaximaDias(10)
                .build();

        EntrenamientoLog entrenamientoLogViejo = EntrenamientoLog.builder()
                .id(1L)
                .usuario(usuario)
                .rutinaEjecutada(rutina)
                .fecha(LocalDate.now())
                .build();

        when(entrenamientoLogRepository.findById(1L)).thenReturn(Optional.of(entrenamientoLogViejo));

        var response = assertThrows(BusinessLogicException.class,
                ()-> entrenamientoLogService.updateEntrenamiento(entrenamientoLogRequest, 1L, infiltrado));

        assertNotNull(response);
        assertEquals("No tenés permiso para editar un entrenamiento que no es tuyo", response.getMessage());
        verify(entrenamientoLogRepository, never()).save(any(EntrenamientoLog.class));
    }

    private EjercicioRutina crearEjercicioRutinaSimulado() {
        DiaRutina diaRutina = DiaRutina
                .builder()
                .id(1L)
                .diaDeLaSemana(DayOfWeek.MONDAY)
                .rutina(rutina)
                .ejercicios(List.of()).build();

        return EjercicioRutina.builder()
                .id(1L)
                .dia(diaRutina)
                .build();
    }
}
package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.GimnasioUpdateRequest;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.GimnasioRepository;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GimnasioServiceTest {

    @Mock
    private GimnasioRepository gimnasioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private GimnasioService gimnasioService;

    @Test
    void adminPuedeEditarUnGimnasioQueNoEsPropio() {
        Usuario propietario = Usuario.builder()
                .id(7L)
                .username("propietario")
                .email("propietario@test.com")
                .rol(Rol.ADMIN_GIMNASIO)
                .build();
        Gimnasio gimnasio = Gimnasio.builder()
                .id(12L)
                .nombre("Gimnasio anterior")
                .direccion("Calle anterior")
                .precioCuota(1000.0)
                .admin(propietario)
                .horarioApertura(LocalTime.of(8, 0))
                .horarioCierre(LocalTime.of(21, 0))
                .diasAbierto(List.of(DayOfWeek.MONDAY))
                .build();
        GimnasioUpdateRequest request = new GimnasioUpdateRequest();
        request.setNombre("Gimnasio actualizado");
        request.setDireccion("Calle actualizada");
        request.setPrecioCuota(1500.0);
        request.setHorarioApertura(LocalTime.of(9, 0));
        request.setHorarioCierre(LocalTime.of(22, 0));
        request.setDiasAbierto(List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY));
        request.setFotoUrl("https://example.test/gym.jpg");

        when(gimnasioRepository.findById(12L)).thenReturn(Optional.of(gimnasio));
        when(gimnasioRepository.save(any(Gimnasio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = gimnasioService.actualizarCualquierGimnasio(request, 12L);

        assertEquals("Gimnasio actualizado", response.getNombre());
        assertEquals("Calle actualizada", response.getDireccion());
        assertEquals(1500.0, response.getPrecioCuota());
        assertEquals(LocalTime.of(9, 0), gimnasio.getHorarioApertura());
        assertEquals(List.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), gimnasio.getDiasAbierto());
        assertEquals("https://example.test/gym.jpg", response.getFotoUrl());
        verify(gimnasioRepository).save(gimnasio);
    }

    @Test
    void adminNoPuedeEditarUnGimnasioInexistente() {
        when(gimnasioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> gimnasioService.actualizarCualquierGimnasio(new GimnasioUpdateRequest(), 404L));
    }
}

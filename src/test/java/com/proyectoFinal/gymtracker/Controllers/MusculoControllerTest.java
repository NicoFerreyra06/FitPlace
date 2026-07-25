package com.proyectoFinal.gymtracker.Controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoFinal.gymtracker.Config.JwtService;
import com.proyectoFinal.gymtracker.Config.SecurityConfig;
import com.proyectoFinal.gymtracker.DTO.Request.MusculoRequest;
import com.proyectoFinal.gymtracker.DTO.Response.MusculoResponse;
import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import com.proyectoFinal.gymtracker.Services.MusculoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MusculoController.class)
@Import(SecurityConfig.class)
public class MusculoControllerTest {

    @MockitoBean
    private MusculoService musculoService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    private MusculoRequest musculoRequest;
    private MusculoResponse musculoResponse;

    @BeforeEach
    void setUp() {
        musculoRequest = MusculoRequest.builder()
                .nombre("musculo")
                .grupoMuscular(GrupoMuscular.DORSAL)
                .build();

        musculoResponse = MusculoResponse.builder()
                .id(1L)
                .nombre("musculo")
                .grupoMuscular(GrupoMuscular.DORSAL)
                .build();
    }

    @Test
    @DisplayName("POST /musculo deberia crear un musculo")
    @WithMockUser(username = "test@gmail.com", roles = "ADMIN")
    void shouldCreateMusculo() throws Exception {
        when(musculoService.addMusculo(any(MusculoRequest.class))).thenReturn(musculoResponse);

        mockMvc.perform(post("/musculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(musculoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("musculo"))
                .andExpect(jsonPath("$.grupoMuscular").value(GrupoMuscular.DORSAL.name()));

        verify(musculoService, times(1)).addMusculo(any(MusculoRequest.class));
    }

    @Test
    @DisplayName("GET /musculo deberia traer el musculo")
    @WithMockUser(username = "test@gmail.com", roles = "ADMIN")
    void shouldGetMusculo() throws Exception {
        when(musculoService.getMusculoById(1L)).thenReturn(musculoResponse);

        mockMvc.perform(get("/musculos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("musculo"));

        verify(musculoService, times(1)).getMusculoById(1L);
    }

    @Test
    @DisplayName("GET /musculos deberia traer todos los musculos")
    @WithMockUser(username = "test@gmail.com", roles = "ADMIN")
    void shouldGetAllMusculos() throws Exception {

        Page<MusculoResponse> pagina = new PageImpl<>(List.of(musculoResponse), PageRequest.of(0, 10), 1);

        when(musculoService.getMusculos(any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/musculos")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].nombre").value("musculo"));

        verify(musculoService, times(1)).getMusculos(any(Pageable.class));
    }

    @Test
    @DisplayName("PUT /musculos deberia actualizar el musculo")
    @WithMockUser(username = "test@gmail.com", roles = "ADMIN")
    void shouldUpdateMusculo() throws Exception {
        when(musculoService.updateMusculo(any(MusculoRequest.class), eq(1L)))
                .thenReturn(musculoResponse);

        mockMvc.perform(put("/musculos/{idMusculo}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(musculoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("musculo"))
                .andExpect(jsonPath("$.grupoMuscular").value(GrupoMuscular.DORSAL.name()));

        verify(musculoService, times(1)).updateMusculo(any(MusculoRequest.class), eq(1L));
    }
}
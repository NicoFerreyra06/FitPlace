package com.proyectoFinal.gymtracker.Config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AjusteEsquemaMusculoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private AjusteEsquemaMusculo ajusteEsquemaMusculo;

    @Test
    @DisplayName("Deberia convertir la columna a VARCHAR si es un ENUM de MySQL")
    void convierteEnum() {
        when(jdbcTemplate.queryForList(AjusteEsquemaMusculo.CONSULTA_TIPO, String.class)).thenReturn(List.of("enum"));

        ajusteEsquemaMusculo.run(null);

        verify(jdbcTemplate).execute(AjusteEsquemaMusculo.CONVERTIR_A_VARCHAR);
    }

    @Test
    @DisplayName("No deberia tocar la columna si ya es VARCHAR")
    void noTocaVarchar() {
        when(jdbcTemplate.queryForList(AjusteEsquemaMusculo.CONSULTA_TIPO, String.class)).thenReturn(List.of("varchar"));

        ajusteEsquemaMusculo.run(null);

        verify(jdbcTemplate, never()).execute(anyString());
    }

    @Test
    @DisplayName("No deberia hacer nada si la tabla todavia no existe")
    void sinTabla() {
        when(jdbcTemplate.queryForList(AjusteEsquemaMusculo.CONSULTA_TIPO, String.class)).thenReturn(List.of());

        ajusteEsquemaMusculo.run(null);

        verify(jdbcTemplate, never()).execute(anyString());
    }
}

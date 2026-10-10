package com.proyectoFinal.gymtracker.Config;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deberia seguir la cadena sin autenticar cuando el token esta vencido")
    void shouldContinueWithoutAuthenticationWhenTokenExpired() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token_vencido");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        when(jwtService.extraerUsername("token_vencido")).thenThrow(new ExpiredJwtException(null, null, "vencido"));

        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertDoesNotThrow(() -> verify(filterChain, times(1)).doFilter(request, response));
    }

    @Test
    @DisplayName("Deberia seguir la cadena sin autenticar cuando el token esta mal formado")
    void shouldContinueWithoutAuthenticationWhenTokenMalformed() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer basura");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        when(jwtService.extraerUsername("basura")).thenThrow(new io.jsonwebtoken.MalformedJwtException("mal formado"));

        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertDoesNotThrow(() -> verify(filterChain, times(1)).doFilter(request, response));
    }

    @Test
    @DisplayName("Deberia seguir la cadena sin autenticar cuando el usuario del token ya no existe")
    void shouldContinueWithoutAuthenticationWhenUserNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token_de_usuario_borrado");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        when(jwtService.extraerUsername("token_de_usuario_borrado")).thenReturn("borrado@test.com");
        when(userDetailsService.loadUserByUsername(anyString())).thenThrow(new UsernameNotFoundException("no existe"));

        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilter(request, response, filterChain));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertDoesNotThrow(() -> verify(filterChain, times(1)).doFilter(request, response));
    }
}

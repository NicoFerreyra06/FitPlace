package com.proyectoFinal.gymtracker.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Importante: Permitir el envío de credenciales/tokens
        config.setAllowCredentials(true);

        // Agregamos la URL donde corre tu React (Vite)
        config.addAllowedOrigin("http://localhost:5173");

        // Permitir todos los headers (Authorization, Content-Type, etc.)
        config.addAllowedHeader("*");

        // Permitir los métodos HTTP que vas a usar
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS"); // El OPTIONS es vital para el "pre-flight" de CORS

        // Aplicar esta regla a todos los endpoints (/**)
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
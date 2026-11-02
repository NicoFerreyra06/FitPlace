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

        config.setMaxAge(3600L);
        config.setAllowCredentials(true);

        config.addAllowedOrigin("http://localhost:4200");

        // permitir todos los headers (Authorization, Content-Type, etc.)
        config.addAllowedHeader("*");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Aplicar esta regla a todos los endpoints (/**)
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
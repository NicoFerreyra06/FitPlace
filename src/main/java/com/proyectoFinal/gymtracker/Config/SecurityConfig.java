package com.proyectoFinal.gymtracker.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/webhook/mercadopago").permitAll()
                        .requestMatchers("/usuarios/registro", "/usuarios/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/ejercicios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/musculos/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/usuarios/me/alumnos").hasRole("ENTRENADOR")
                        .requestMatchers(HttpMethod.GET, "/usuarios").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/ejercicios/**", "/musculos/**", "/gimnasios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/gimnasios/me").hasRole("ADMIN_GIMNASIO")

                        .requestMatchers(HttpMethod.PUT, "/ejercicios/**", "/musculos/**", "/gimnasios/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/ejercicios/**", "/musculos/**").hasRole("ADMIN")

                        .requestMatchers("/rutinas/**", "/entrenamientos/**").authenticated()
                        .requestMatchers("/usuarios/**").authenticated()

                        .anyRequest().authenticated()
                )
                .build();
    }

}
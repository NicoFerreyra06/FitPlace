package com.proyectoFinal.gymtracker.Config;

import com.proyectoFinal.gymtracker.Enum.Rol;
import jakarta.servlet.http.HttpServletResponse;
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

                // Sin autenticar respondemos 401 (el 403 queda para quien está logueado pero sin rol suficiente)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/webhook/mercadopago").permitAll()
                        .requestMatchers("/mercadopago/callback").permitAll()
                        .requestMatchers("/usuarios/registro", "/usuarios/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/ejercicios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/musculos/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/usuarios/me/alumnos").hasRole(Rol.ENTRENADOR.name())
                        .requestMatchers(HttpMethod.DELETE, "/usuarios/me/alumnos/**").hasRole(Rol.ENTRENADOR.name())
                        .requestMatchers(HttpMethod.GET, "/entrenamientos/alumno/**").hasRole(Rol.ENTRENADOR.name())
                        .requestMatchers(HttpMethod.GET, "/usuarios", "/usuarios/{id:[0-9]+}").hasRole(Rol.ADMIN.name())

                        .requestMatchers(HttpMethod.POST, "/ejercicios/**", "/musculos/**", "/gimnasios").hasRole(Rol.ADMIN.name())
                        .requestMatchers(HttpMethod.PUT, "/gimnasios/me").hasRole(Rol.ADMIN_GIMNASIO.name())

                        .requestMatchers(HttpMethod.PUT, "/ejercicios/**", "/musculos/**", "/gimnasios/{id}").hasRole(Rol.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/ejercicios/**", "/musculos/**").hasRole(Rol.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/gimnasios/**").hasAnyRole(Rol.ADMIN.name(), Rol.ADMIN_GIMNASIO.name())

                        .requestMatchers("/rutinas/**", "/entrenamientos/**").authenticated()
                        .requestMatchers("/usuarios/**").authenticated()

                        .anyRequest().authenticated()
                )
                .build();
    }

}
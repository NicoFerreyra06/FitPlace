package com.proyectoFinal.gymtracker.Config;

import com.proyectoFinal.gymtracker.Exception.UserNotFoundException;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UsuarioRepository usuarioRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> (org.springframework.security.core.userdetails.UserDetails) usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + email));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/usuarios/registro", "/usuarios/login").permitAll()

                       .requestMatchers(HttpMethod.GET, "/ejercicios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/musculos/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/ejercicios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/ejercicios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/ejercicios/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/musculos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/musculos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/musculos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/gimnasios").authenticated()
                        .requestMatchers("/rutinas/**").authenticated()
                        .requestMatchers("/entrenamientos/**").authenticated()

                        .requestMatchers("/usuarios/**").authenticated()

                        .anyRequest().authenticated()
                )
                .httpBasic(org.springframework.security.config.Customizer.withDefaults())
                .build();
    }



}
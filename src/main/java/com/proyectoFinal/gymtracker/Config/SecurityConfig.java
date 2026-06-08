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
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/ejercicios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/musculos/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/usuarios/me/alumnos").hasRole("ENTRENADOR")
                        .requestMatchers(HttpMethod.GET, "/usuarios/").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/ejercicios/**", "/musculos/**", "/gimnasios").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/ejercicios/**", "/musculos/**", "/gimnasios/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/ejercicios/**", "/musculos/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PUT, "/gimnasios/me").hasRole("ADMIN_GIMNASIO")


                        .requestMatchers("/rutinas/**", "/entrenamientos/**").authenticated()
                        .requestMatchers("/usuarios/**").authenticated()

                        .anyRequest().authenticated()
                )
                .httpBasic(org.springframework.security.config.Customizer.withDefaults())
                .build();
    }

}
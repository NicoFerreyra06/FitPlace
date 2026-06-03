package com.proyectoFinal.gymtracker.Modelo;

import com.proyectoFinal.gymtracker.Enum.Rol;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Rol rol;

    private Double peso;
    private Double altura;

    @Column(unique = true, nullable = false, length = 36)
    private String codigoAmigo; // id para compartir

    @ManyToMany
    @JoinTable(
            name = "amigos",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "amigo_id")
    )
    @JsonIgnore
    private List<Usuario> amigos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rutina_activa_id")
    private Rutina rutinaActiva;

    private LocalDate rutinaActivaDesde; //necesario para a futuro ver progreso desde que uso tal rutina


    // Relación para que un usuario pueda tener un entrenador
    @ManyToOne
    @JoinColumn(name = "entrenador_id")
    @JsonIgnore
    private Usuario entrenador;

    // Un entrenador puede tener muchos clientes
    @JsonIgnore
    @OneToMany(mappedBy = "entrenador", fetch = FetchType.LAZY)
    private List<Usuario> clientes;

    // La racha se podría calcular dinámicamente con EntrenamientoLog,
    // pero guardamos un caché de la racha actual para mostrarla rápido.
    private Integer rachaActualDias;
    private Integer rachaMaximaDias;

    @Transient //para que hibernatee no lo quiera guardar en db
    public Double getImc() {
        if (peso != null && altura != null && altura > 0) {
            return peso / (altura * altura);
        }
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
        }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}
package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.Enum.EstadoSuscripcion;
import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import com.proyectoFinal.gymtracker.Modelo.SuscripcionGimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SuscripcionGimnasioRepository extends JpaRepository<SuscripcionGimnasio, Long> {

    boolean existsByUsuarioAndEstadoSuscripcionIn(
            Usuario usuario,
            List<EstadoSuscripcion> estados
    );

    Optional<SuscripcionGimnasio> findByUsuarioAndEstadoSuscripcion(Usuario usuario, EstadoSuscripcion estadoSuscripcion);

    Optional<SuscripcionGimnasio> findByIdAndUsuario(Long id, Usuario usuario);

    List<SuscripcionGimnasio> findByEstadoSuscripcionAndFechaFinBefore(EstadoSuscripcion estadoSuscripcion, LocalDate hoy);

    Optional<SuscripcionGimnasio> findFirstByUsuarioAndEstadoSuscripcionIn(Usuario usuario, List<EstadoSuscripcion> estados);
}

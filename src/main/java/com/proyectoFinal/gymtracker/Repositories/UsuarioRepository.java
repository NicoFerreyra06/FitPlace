package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByCodigoAmigo(String codigoAmigo);

    List<Usuario> findByEntrenadorId(Long entrenadorId);

    List<Usuario> findByGimnasioId(Long gimnasioId);
}
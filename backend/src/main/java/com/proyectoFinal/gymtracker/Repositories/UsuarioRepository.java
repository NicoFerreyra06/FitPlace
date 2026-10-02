package com.proyectoFinal.gymtracker.Repositories;

import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByCodigoAmigo(String codigoAmigo);

    List<Usuario> findByEntrenadorId(Long entrenadorId);

    @Query("SELECT u from Usuario u where u.gimnasio.id = :gimnasioId")
    List<Usuario> findAllByGimnasioId(Long gimnasioId);

    @Query("SELECT a from Usuario u  join u.amigos a where u.id = :userId")
    List<Usuario> findAllFriendByUserId(Long userId);

    List<Usuario> findByRol(Rol rol);
}
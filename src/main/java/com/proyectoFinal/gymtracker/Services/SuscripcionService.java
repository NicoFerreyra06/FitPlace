package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.Enum.EstadoSuscripcion;
import com.proyectoFinal.gymtracker.Enum.MetodoPago;
import com.proyectoFinal.gymtracker.Enum.Rol;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.UserNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Gimnasio;
import com.proyectoFinal.gymtracker.Modelo.SuscripcionGimnasio;
import com.proyectoFinal.gymtracker.Modelo.Usuario;
import com.proyectoFinal.gymtracker.Repositories.GimnasioRepository;
import com.proyectoFinal.gymtracker.Repositories.SuscripcionGimnasioRepository;
import com.proyectoFinal.gymtracker.Repositories.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuscripcionService {

    private final GimnasioRepository gimnasioRepository;
    private final SuscripcionGimnasioRepository suscripcionGimnasioRepository;
    private final UsuarioRepository usuarioRepository;

    public SuscripcionGimnasio createSuscripcion(Long idGimnasio, Usuario usuario) {
        Gimnasio gimnasio = gimnasioRepository.findById(idGimnasio)
                .orElseThrow(() -> new BusinessLogicException("Gimnasio no encontrado"));

        if (suscripcionGimnasioRepository.existsByUsuarioAndEstadoSuscripcionIn(
                usuario,
                List.of(
                        EstadoSuscripcion.ACTIVA,
                        EstadoSuscripcion.PENDIENTE
                )
        )) {
            throw new BusinessLogicException(
                    "Ya posee una suscripción activa o pendiente"
            );
        }

        SuscripcionGimnasio suscripcionGimnasio = SuscripcionGimnasio
                .builder()
                .gimnasio(gimnasio)
                .usuario(usuario)
                .fechaInicio(null)
                .fechaFin(null)
                .costo(gimnasio.getPrecioCuota())
                .estadoSuscripcion(EstadoSuscripcion.PENDIENTE)
                .comisionApp(gimnasio.getPrecioCuota() * 0.1).build();

        return suscripcionGimnasioRepository.save(suscripcionGimnasio);
    }

    @Transactional
    public SuscripcionGimnasio activarSuscripcion(Long idSuscripcion, Usuario adminAutenticado) {

        SuscripcionGimnasio suscripcion = suscripcionGimnasioRepository.findById(idSuscripcion)
                .orElseThrow(() -> new BusinessLogicException("Suscripción no encontrada"));

        if (adminAutenticado.getRol() != Rol.ADMIN) {
            if (adminAutenticado.getGimnasio() == null ||
                    !adminAutenticado.getGimnasio().getId().equals(suscripcion.getGimnasio().getId())) {
                throw new BusinessLogicException("No tienes permisos para administrar las suscripciones de este gimnasio");
            }
        }

        Usuario cliente = suscripcion.getUsuario();

        if (suscripcion.getEstadoSuscripcion() != EstadoSuscripcion.PENDIENTE) {
            throw new BusinessLogicException("La suscripción no se encuentra pendiente");
        }

        if (cliente.getGimnasio() != null) {
            throw new BusinessLogicException("El usuario ya pertenece a un gimnasio");
        }

        suscripcion.setEstadoSuscripcion(EstadoSuscripcion.ACTIVA);
        suscripcion.setFechaInicio(LocalDate.now());
        suscripcion.setFechaFin(LocalDate.now().plusMonths(1));
        suscripcion.setMetodoPago(MetodoPago.EFECTIVO);

        cliente.setGimnasio(suscripcion.getGimnasio());

        return suscripcion;
    }

    public SuscripcionGimnasio getSuscripcionGimnasio(Usuario usuario) {

        return suscripcionGimnasioRepository.findByUsuarioAndEstadoSuscripcion(usuario,
                EstadoSuscripcion.ACTIVA)
                .orElseThrow(() -> new BusinessLogicException("Suscripción activa no encontrada"));
    }

    @Transactional
    public SuscripcionGimnasio cancelarSuscripcion(Long idSuscripcion, Usuario usuario) {

        SuscripcionGimnasio suscripcion = suscripcionGimnasioRepository.findById(idSuscripcion)
                .orElseThrow(() -> new BusinessLogicException("Suscripción no encontrada"));

        if (!suscripcion.getUsuario().getId().equals(usuario.getId())) {
            throw new BusinessLogicException("No puede cancelar esta suscripción");
        }

        if (suscripcion.getEstadoSuscripcion() == EstadoSuscripcion.CANCELADA) {
            throw new BusinessLogicException("La suscripción ya está cancelada");
        }

        if (suscripcion.getEstadoSuscripcion() == EstadoSuscripcion.VENCIDA) {
            throw new BusinessLogicException("La suscripción ya está vencida");
        }

        suscripcion.setEstadoSuscripcion(EstadoSuscripcion.CANCELADA);

        Usuario u = suscripcion.getUsuario();
        u.setGimnasio(null);

        return suscripcion;
    }

    public SuscripcionGimnasio getByIdAndUser (Long idSuscripcion, Usuario usuario) {
        return suscripcionGimnasioRepository.findByIdAndUsuario(idSuscripcion, usuario)
                .orElseThrow(() -> new BusinessLogicException("Suscripcion no encontrada"));
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void actualizarSuscripciones() {
        LocalDate hoy = LocalDate.now();

        List<SuscripcionGimnasio> suscripcionesVencidas = suscripcionGimnasioRepository
                .findByEstadoSuscripcionAndFechaFinBefore(EstadoSuscripcion.ACTIVA, hoy);

        if (!suscripcionesVencidas.isEmpty()) {
            for (SuscripcionGimnasio suscripcion : suscripcionesVencidas) {
                suscripcion.setEstadoSuscripcion(EstadoSuscripcion.VENCIDA);

                Usuario usuario = suscripcion.getUsuario();
                usuario.setGimnasio(null);
            }
        }
    }
}

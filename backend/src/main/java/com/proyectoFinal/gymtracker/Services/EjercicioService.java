package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.EjercicioRequest;
import com.proyectoFinal.gymtracker.DTO.Response.EjercicioResponse;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Ejercicio;
import com.proyectoFinal.gymtracker.Modelo.Musculo;
import com.proyectoFinal.gymtracker.Repositories.EjercicioRepository;
import com.proyectoFinal.gymtracker.Repositories.MusculoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EjercicioService {

    private final EjercicioRepository ejercicioRepository;
    private final MusculoRepository musculoRepository;

    public EjercicioResponse addEjercicio(EjercicioRequest request) {
        List<Musculo> principales = request.getMusculoPrincipalId() != null
                ? musculoRepository.findAllById(request.getMusculoPrincipalId())
                : List.of();
        List<Musculo> secundarios = request.getMusculoSecundarioId() != null && !request.getMusculoSecundarioId().isEmpty()
                ? musculoRepository.findAllById(request.getMusculoSecundarioId())
                : List.of();

        Ejercicio ejercicio = Ejercicio.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .activo(true)
                .musculosPrincipales(principales)
                .musculosSecundarios(secundarios)
                .build();

        Ejercicio saved = ejercicioRepository.save(ejercicio);
        return toResponse(saved);
    }

    public List<EjercicioResponse> addEjercicios(List<EjercicioRequest> requests) {
        List<Ejercicio> ejercicios = requests.stream()
                .map(request -> {
                    List<Musculo> principales = request.getMusculoPrincipalId() != null
                            ? musculoRepository.findAllById(request.getMusculoPrincipalId())
                            : List.of();
                    List<Musculo> secundarios = request.getMusculoSecundarioId() != null && !request.getMusculoSecundarioId().isEmpty()
                            ? musculoRepository.findAllById(request.getMusculoSecundarioId())
                            : List.of();
                    return Ejercicio.builder()
                            .nombre(request.getNombre())
                            .descripcion(request.getDescripcion())
                            .activo(true)
                            .musculosPrincipales(principales)
                            .musculosSecundarios(secundarios)
                            .build();
                }).toList();

        return ejercicioRepository.saveAll(ejercicios).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EjercicioResponse updateEjercicio(EjercicioRequest ejercicioRequest, Long idEjercicio) {
        Ejercicio ejercicio = ejercicioRepository.findById(idEjercicio)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

        List<Musculo> principales = ejercicioRequest.getMusculoPrincipalId() != null
                ? musculoRepository.findAllById(ejercicioRequest.getMusculoPrincipalId())
                : List.of();
        List<Musculo> secundarios = ejercicioRequest.getMusculoSecundarioId() != null && !ejercicioRequest.getMusculoSecundarioId().isEmpty()
                ? musculoRepository.findAllById(ejercicioRequest.getMusculoSecundarioId())
                : List.of();

        ejercicio.setNombre(ejercicioRequest.getNombre());
        ejercicio.setDescripcion(ejercicioRequest.getDescripcion());
        ejercicio.setMusculosPrincipales(principales);
        ejercicio.setMusculosSecundarios(secundarios);

        return toResponse(ejercicioRepository.save(ejercicio));
    }

    public EjercicioResponse getById(Long id) {
        Ejercicio saved = ejercicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

        return toResponse(saved);
    }

    public Page<EjercicioResponse> getAll(Pageable pageable) {
        return ejercicioRepository.findAllByActivoTrue(pageable)
                .map(this::toResponse);
    }

    public void deleteEjercicio(Long id) {
        Ejercicio ejercicio = ejercicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado"));

        ejercicio.setActivo(false);
        ejercicioRepository.save(ejercicio);
    }

    public List<EjercicioResponse> getByMusculo(Long musculoId) {
        return ejercicioRepository.findByMusculo(musculoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EjercicioResponse> getByMusculoPrincipal(Long musculoId) {
        return ejercicioRepository.findByMusculoPrincipal(musculoId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EjercicioResponse> buscarPorNombre(String nombre) {
        return ejercicioRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EjercicioResponse toResponse(Ejercicio ejercicio) {
        return EjercicioResponse.builder()
                .id(ejercicio.getId())
                .nombre(ejercicio.getNombre())
                .descripcion(ejercicio.getDescripcion())
                .activo(ejercicio.isActivo())
                .musculosPrincipales(ejercicio.getMusculosPrincipales())
                .musculosSecundarios(ejercicio.getMusculosSecundarios())
                .build();
    }
}
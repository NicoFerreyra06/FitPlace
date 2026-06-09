package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.DTO.Request.MusculoRequest;
import com.proyectoFinal.gymtracker.DTO.Response.MusculoResponse;
import com.proyectoFinal.gymtracker.Enum.GrupoMuscular;
import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Musculo;
import com.proyectoFinal.gymtracker.Repositories.MusculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusculoService {

    private final MusculoRepository musculoRepository;

    public MusculoResponse addMusculo(MusculoRequest request){
        Musculo musculo = requestToMusculo(request);

        return musculoToResponse(musculoRepository.save(musculo));
    }

    public List<MusculoResponse> addMusculos (List<MusculoRequest> musculosRequests){

        List<Musculo> musculosList = musculosRequests.stream().map(this::requestToMusculo).toList();

        return musculoRepository.saveAll(musculosList).stream().map(this::musculoToResponse).toList();
    }

    public void deleteMusculos(Long idMusculo){
        try{
            musculoRepository.deleteById(idMusculo);
        } catch (DataIntegrityViolationException e){
            throw new BusinessLogicException("No se puede eliminar el musculo porque esta relacionado a ejercicios");
        }

    }

    public MusculoResponse getMusculoById(Long idMusculo){
        Musculo musculo = musculoRepository.findById(idMusculo)
                .orElseThrow(() -> new ResourceNotFoundException("Musculo no encontrado"));


        return musculoToResponse(musculo);
    }

    public Page<MusculoResponse> getMusculos(Pageable pageable){
        return musculoRepository.findAll(pageable).map(this::musculoToResponse);
    }

    public MusculoResponse updateMusculo(MusculoRequest request, Long idMusculo){
        Musculo musculoExistente = musculoRepository.findById(idMusculo)
                .orElseThrow(() -> new ResourceNotFoundException("Musculo no encontrado"));

        musculoExistente.setNombre(request.getNombre());
        musculoExistente.setGrupoMuscular(request.getGrupoMuscular());

        return musculoToResponse(musculoRepository.save(musculoExistente));
    }

    public List<MusculoResponse> getByGrupoMuscular(GrupoMuscular grupoMuscular){
        return musculoRepository.findByGrupoMuscular(grupoMuscular)
                .stream().map(this::musculoToResponse).toList();
    }

    private Musculo requestToMusculo(MusculoRequest request){
        return Musculo.builder()
                .nombre(request.getNombre()).grupoMuscular(request.getGrupoMuscular()).build();
    }

    private MusculoResponse musculoToResponse(Musculo musculo){
        return MusculoResponse.builder()
                .id(musculo.getId())
                .nombre(musculo.getNombre())
                .grupoMuscular(musculo.getGrupoMuscular()).build();
    }
}

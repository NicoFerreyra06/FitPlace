package com.proyectoFinal.gymtracker.Services;

import com.proyectoFinal.gymtracker.Exception.BusinessLogicException;
import com.proyectoFinal.gymtracker.Exception.ResourceNotFoundException;
import com.proyectoFinal.gymtracker.Modelo.Musculo;
import com.proyectoFinal.gymtracker.Repositories.MusculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusculoService {

    private final MusculoRepository musculoRepository;

    public Musculo addMusculo(Musculo musculo){
        return musculoRepository.save(musculo);
    }

    public List<Musculo> addMusculos (List<Musculo> musculos){
        return musculoRepository.saveAll(musculos);
    }

    public void deleteMusculos(Long idMusculo){
        try{
            musculoRepository.deleteById(idMusculo);
        } catch (DataIntegrityViolationException e){
            throw new BusinessLogicException("No se puede eliminar el musculo porque esta relacionado a ejercicios");
        }

    }

    public Musculo getMusculoById(Long idMusculo){
        return musculoRepository.findById(idMusculo)
                .orElseThrow(()-> new ResourceNotFoundException("Musculo no encontrado"));
    }

    public List<Musculo> getMusculos(){
        return musculoRepository.findAll();
    }

    public Musculo updateMusculo(Musculo musculo){
        Musculo musculoExistente = musculoRepository.findById(musculo.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Musculo no encontrado"));

        musculoExistente.setNombre(musculo.getNombre());
        return musculoRepository.save(musculoExistente);
    }
}

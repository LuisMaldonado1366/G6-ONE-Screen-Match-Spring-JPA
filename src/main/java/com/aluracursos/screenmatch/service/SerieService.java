package com.aluracursos.screenmatch.service;

import com.aluracursos.screenmatch.dto.SerieDTO;
import com.aluracursos.screenmatch.model.Serie;
import com.aluracursos.screenmatch.repository.SerieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SerieService {

    @Autowired
    private SerieRepository repository;

    public List<SerieDTO> obtenerTodasLasSeries() {

        return convierteDatos(repository.findAll());

    }

    public List<SerieDTO> obtenerTopCinco() {

        return convierteDatos(repository.findTop5ByOrderByEvaluacionDesc());

    }

    public List<SerieDTO> obtenerLanzamientosMasRecientes() {

        return convierteDatos(repository.lanzamientosMasrecientes());

    }

    public List<SerieDTO> convierteDatos(List<Serie> seriesEntrada) {
        return seriesEntrada.stream().
                map(serie -> new SerieDTO(serie.getId(), serie.getTitulo(), serie.getTotalTemporadas(), serie.getEvaluacion(),
                serie.getPoster(), serie.getGenero(), serie.getActores(), serie.getSinopsis()))
                .collect(Collectors.toList());
    }

    public SerieDTO obtenerPorId(Long id) {

        Optional<Serie> serieId = repository.findById(id);

        if (serieId.isPresent()){
            Serie serie = serieId.get();
            return new SerieDTO(serie.getId(), serie.getTitulo(), serie.getTotalTemporadas(), serie.getEvaluacion(),
                    serie.getPoster(), serie.getGenero(), serie.getActores(), serie.getSinopsis());
        }

        return null;
    }
}

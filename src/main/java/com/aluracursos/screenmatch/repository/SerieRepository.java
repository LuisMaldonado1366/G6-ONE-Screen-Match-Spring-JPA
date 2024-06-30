package com.aluracursos.screenmatch.repository;

import com.aluracursos.screenmatch.model.Categoria;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {

    Optional<Serie> findByTituloContainsIgnoreCase(String nombreSerie);

    List<Serie> findTop5ByOrderByEvaluacionDesc();
    List<Serie> findByGenero(Categoria categoria);
    @Query("SELECT serie FROM Serie serie WHERE serie.totalTemporadas <= :totalTemporadas AND serie.evaluacion >= :evaluacion")
    List<Serie> filtrarTotalTemporadasYEvaluacion(int totalTemporadas, Double evaluacion);

    @Query("SELECT e, s.titulo FROM Serie s JOIN s.episodios e WHERE e.titulo ILIKE %:nombreEpisodio%")
    List<Episodio> episodiosPorNombre(String nombreEpisodio);

    @Query("SELECT e FROM Serie AS s JOIN s.episodios AS e WHERE s = :serie ORDER BY e.evaluacion DESC LIMIT 5")
    List<Episodio> topCincoEpisodios(Serie serie);

    @Query("SELECT s FROM Serie AS s JOIN s.episodios AS e GROUP BY s ORDER BY MAX(e.fechaDeLanzamiento) DESC LIMIT 5")
    List<Serie> lanzamientosMasrecientes();
}

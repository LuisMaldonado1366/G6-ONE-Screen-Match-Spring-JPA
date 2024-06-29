package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Principal {
    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoApi = new ConsumoAPI();
    private final Dotenv dotenv = Dotenv.load();
    private final String URL_BASE = "https://www.omdbapi.com/?apikey=" + dotenv.get("OMDB_TOKEN") + "&t=";
    private final ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();
    private SerieRepository serieRepository;
    private List<Serie> series;
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repository) {
        this.serieRepository = repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    \n\n
                    1 - Buscar series 
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Buscar series por título
                    5 - Top 5 mejores series
                    6 - Buscar series por categoria
                    7 - Filtrar series
                    8 - Buscar episodios por titulo
                    9 - Top 5 episodios por serie
                                  
                    0 - Salir
                    """;
            System.out.println(menu);
            String entradaUsuario = teclado.nextLine();
            try {
                opcion = Integer.parseInt(entradaUsuario);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un valor numérico.");
                opcion = -1;
            }

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriesPorTitulo();
                    break;
                case 5:
                    buscarTop5Series();
                    break;
                case 6:
                    buscarSeriesPorCategoria();
                    break;
                case 7:
                    filtrarSeriesPorTemporadaYEvaluacion();
                    break;
                case 8:
                    buscarEpisodiosPorTitulo();
                    break;
                case 9:
                    buscarTopCincoEpisodios();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida\n");
            }
        }
    }


    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + URLEncoder.encode(nombreSerie, StandardCharsets.UTF_8));
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }
    private void buscarEpisodioPorSerie() {
        mostrarSeriesBuscadas();
        System.out.println("Escribe el nombre de la serie que deseas ver los episodios");
        String nombreSerie = teclado.nextLine();

        Optional<Serie> serie = series.stream()
                .filter(s -> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();
        if (serie.isPresent()){
            var serieEncontrada = serie.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + URLEncoder.encode(serieEncontrada.getTitulo(), StandardCharsets.UTF_8) + "&season=" + i);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(temporada -> temporada.episodios().stream()
                            .map(episodio -> new Episodio(temporada.numero(), episodio)))
                    .toList();
            serieEncontrada.setEpisodios(episodios);
            serieRepository.save(serieEncontrada);
        }



    }
    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie = new Serie(datos);
        serieRepository.save(serie);
//        datosSeries.add(datos);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {

        series = serieRepository.findAll();

        try {
            series.stream()
                    .sorted(Comparator.comparing(Serie::getGenero))
                    .forEach(System.out::println);
        } catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
        }

    }

    private void buscarSeriesPorTitulo() {

        System.out.println("Escribe el nombre de la serie que deseas buscar:");
        String nombreSerie = teclado.nextLine();
        serieBuscada = serieRepository.findByTituloContainsIgnoreCase(nombreSerie);

        if( serieBuscada.isPresent()) {
            System.out.println("La serie buscada es: \"" + serieBuscada.get() + "\"");
        } else {
            System.out.println("Ninguna coincidencia para la serie \"" + nombreSerie + "\"");
        }

    }

    private void buscarTop5Series() {
        List<Serie> topSeries = serieRepository.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(serie ->
                System.out.printf("\nSerie: %s - Evaluacion: %f",serie.getTitulo(), serie.getEvaluacion()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Escriba el genero de la serie que desea buscar: ");
        String genero = teclado.nextLine();
        try {
            Categoria categoria = Categoria.fromEspanol(genero);
            List<Serie> seriesPorCategoria = serieRepository.findByGenero(categoria);
            System.out.println("\nLas series de la categoría " + genero);
            seriesPorCategoria.forEach(System.out::println);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

    }

    private void filtrarSeriesPorTemporadaYEvaluacion() {
        int totalTemporadas = 0;
        Double evaluacion = 0.0;

        System.out.println("Ingrese el número de temporadas: ");
        String entradaTemporadas = teclado.nextLine();
        System.out.println("Ingrese la evaluación: ");
        String entradaEvaluacion = teclado.nextLine();

        try {
            totalTemporadas = Integer.parseInt(entradaTemporadas);
            evaluacion = Double.parseDouble(entradaEvaluacion);
        } catch (NumberFormatException e) {
            System.out.println("Ingrese un valor numérico.");
        }

        List<Serie> seriesFiltradas = serieRepository.filtrarTotalTemporadasYEvaluacion(totalTemporadas, evaluacion);
        System.out.println("\nLas series encontradas son: ");
        seriesFiltradas.forEach(serie ->
                System.out.println("* " + serie.getTitulo() +
                        "\n\tEvaluación: " + serie.getEvaluacion() +
                        "\n\tTemporadas: " + serie.getTotalTemporadas()));
    }

    private void buscarEpisodiosPorTitulo() {
        System.out.println("Escriba el nombre del episodio que desea buscar: ");
        String nombreEpisodio = teclado.nextLine();
        List<Episodio> episodios = serieRepository.episodiosPorNombre(nombreEpisodio);

        episodios.forEach(episodio ->
                System.out.printf("\n* Serie: %s" +
                        "\n\tTemporada: %s " +
                        "\n\tEpisodio: %s " +
                        "\n\tFecha de lanzamiento: %s" +
                        "\n\tEvaluacion: %02f", episodio.getSerie().getTitulo(),
                        episodio.getTemporada(), episodio.getTitulo(),
                        episodio.getFechaDeLanzamiento(),
                        episodio.getEvaluacion()));
    }

    private void buscarTopCincoEpisodios() {
        buscarSeriesPorTitulo();
        if( serieBuscada.isPresent()) {
            Serie serie = serieBuscada.get();
            List<Episodio> topEpisodios = serieRepository.topCincoEpisodios(serie);
            System.out.print("\n* Serie: " + serie.getTitulo());
            int index = 0;
            topEpisodios.forEach( episodio ->
                    System.out.printf("\n\t(%d) Titulo: %s " +
                            "\n\t  Temporada: %d" +
                            "\n\t  Numero de episodio: %d" +
                            "\n\t  Fecha de lanzamiento: %s" +
                            "\n\t  Evaluacion: %02f\n", index++, episodio.getTitulo(), episodio.getTemporada(),
                            episodio.getNumeroEpisodio(), episodio.getFechaDeLanzamiento(), episodio.getEvaluacion()));
        }

    }



}


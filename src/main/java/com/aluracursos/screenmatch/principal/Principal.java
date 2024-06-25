package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.DatosSerie;
import com.aluracursos.screenmatch.model.DatosTemporadas;
import com.aluracursos.screenmatch.model.Serie;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoApi = new ConsumoAPI();
    private final Dotenv dotenv = Dotenv.load();
    private final String URL_BASE = "https://www.omdbapi.com/?apikey=" + dotenv.get("OMDB_TOKEN") + "&t=";
    private final ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie> datosSeries = new ArrayList<>();

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series 
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                                  
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
        DatosSerie datosSerie = getDatosSerie();
        List<DatosTemporadas> temporadas = new ArrayList<>();

        for (int i = 1; i <= datosSerie.totalTemporadas(); i++) {
            var json = consumoApi.obtenerDatos(URL_BASE + URLEncoder.encode(datosSerie.titulo(), StandardCharsets.UTF_8) + "&season=" + i);
            DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
            temporadas.add(datosTemporada);
        }
        temporadas.forEach(System.out::println);
    }
    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        datosSeries.add(datos);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscadas() {

        List<Serie> series = new ArrayList<>();
        series = datosSeries.stream()
                .map(Serie::new)
                .collect(Collectors.toList());

        try {
            series.stream()
                    .sorted(Comparator.comparing(Serie::getGenero))
                    .forEach(System.out::println);
        } catch (IllegalArgumentException e){
            System.out.println(e.getMessage());
        }

    }


}


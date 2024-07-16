package com.alura.literalura.principal;

import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;
import com.alura.literalura.model.*;
import java.util.stream.Collectors;
import java.util.*;

public class Principal {
private Scanner teclado = new Scanner(System.in);
private ConvierteDatos conversor = new ConvierteDatos();
private ConsumoAPI consumoAPI = new ConsumoAPI();
private AutorRepository repository;
private String URL_BASE = "https://gutendex.com/books/";

public Principal(AutorRepository repository){
    this.repository = repository;
}

public void muestraMenu() {
    var opcion = 6;
    var menu = """
            --- Menú ---
            Elija la opción a través de su número:
            1 - Buscar libro por tÍtulo
            2 - Listar libros registrados
            3 - Listar autores registrados
            4 - Listar autores vivos en un determinado año
            5 - Listar libros por idioma
            0 - Salir
            """;

    while (opcion != 0) {
        System.out.println(menu);
        try {
            opcion = Integer.valueOf(teclado.nextLine());
            switch (opcion) {
                case 1: buscaLibroPorTitulo(); break;
                case 2: listaLibrosRegistrados(); break;
                case 3: listaAutoresRegistrados(); break;
                case 4: listaAutoresVivos(); break;
                case 5: listaLibrosPorIdioma(); break;
                case 0: System.out.println("Saliendo..."); break;
                default: System.out.println("Elija una opción válida."); break;
            }
        } catch (NumberFormatException e) {
            System.out.println("Elija una opción válida: " + e.getMessage());

        }
    }
}

public void buscaLibroPorTitulo() {
    System.out.println("--- Buscar libro por tÍtulo ---");
    System.out.println("Indique título a buscar:");
    var nombre = teclado.nextLine();
    var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombre.replace(" ", "+").toLowerCase());

    if (json.isEmpty() || !json.contains("\"count\":0,\"next\":null,\"previous\":null,\"results\":[]")) {
        var datos = conversor.obtenerDatos(json, Datos.class);

        // Process valid data
        Optional<DatosLibro> libroBuscado = datos.libros().stream()
                .findFirst();
        if (libroBuscado.isPresent()) {
            System.out.println(
                            "\nTítulo: " + libroBuscado.get().titulo() +
                            "\nAutor: " + libroBuscado.get().autores().stream()
                            .map(a -> a.nombre()).limit(1).collect(Collectors.joining()) +
                            "\nIdioma: " + libroBuscado.get().idiomas().stream().collect(Collectors.joining()) +
                            "\nDescargas: " + libroBuscado.get().descargas() + "\n"
            );

            try {
                List<Libro> libroEncontrado = libroBuscado.stream().map(a -> new Libro(a)).collect(Collectors.toList());
                Autor autorAPI = libroBuscado.stream().
                        flatMap(l -> l.autores().stream()
                                .map(a -> new Autor(a)))
                        .collect(Collectors.toList()).stream().findFirst().get();
                Optional<Autor> autorBD = repository.buscarAutorPorNombre(libroBuscado.get().autores().stream()
                        .map(a -> a.nombre())
                        .collect(Collectors.joining()));
                Optional<Libro> libroOptional = repository.buscarLibroPorNombre(nombre);
                if (libroOptional.isPresent()) {
                    System.out.println("El libro existe en la base de datos.");
                } else {
                    Autor autor;
                    if (autorBD.isPresent()) {
                        autor = autorBD.get();
                        System.out.println("El autor existe en la base de datos.");
                    } else {
                        autor = autorAPI;
                        repository.save(autor);
                    }
                    autor.setLibros(libroEncontrado);
                    repository.save(autor);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("El libro NO existe en la base de datos.");
        }
    }
}

public void listaLibrosRegistrados () {
    System.out.println("--- Listar libros registrados ---");
    List<Libro> libros = repository.buscaTodosLosLibros();
    libros.forEach(l -> System.out.println(
                    "\nTítulo: " + l.getTitulo() +
                    "\nAutor: " + l.getAutor().getNombre() +
                    "\nIdioma: " + l.getIdioma().getIdioma() +
                    "\nNúmero de descargas: " + l.getDescargas() + "\n"
    ));
}

public void listaAutoresRegistrados () {
    System.out.println("--- Listar autores registrados ---");
    List<Autor> autores = repository.findAll();
    System.out.println();
    autores.forEach(l -> System.out.println(
                    "Autor: " + l.getNombre() +
                    "\nFecha de Nacimiento: " + l.getNacimiento() +
                    "\nFecha de Fallecimiento: " + l.getFallecimiento() +
                    "\nLibros: " + l.getLibros().stream()
                    .map(t -> t.getTitulo()).collect(Collectors.toList()) + "\n"
    ));
}

public void listaAutoresVivos () {
    System.out.println("--- Listar autores vivos en un determinado año ---");
    System.out.println("Indique el año:");
    try {
        var fecha = Integer.valueOf(teclado.nextLine());
        List<Autor> autores = repository.buscarAutoresVivos(fecha);
        if (!autores.isEmpty()) {
            System.out.println();
            autores.forEach(a -> System.out.println(
                            "\nAutor: " + a.getNombre() +
                            "\nFecha de Nacimiento: " + a.getNacimiento() +
                            "\nFecha de Fallecimiento: " + a.getFallecimiento() +
                            "\nLibros: " + a.getLibros().stream()
                            .map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
            ));
        } else {
            System.out.println("No se encontraron autores vivos en ese año.");
        }
    } catch (NumberFormatException e) {
        System.out.println("Indique año válido " + e.getMessage());
    }
}

public void listaLibrosPorIdioma() {
System.out.println("--- Listar libros por idioma ---");
var menu = """
            --- Elija el idioma de su preferencia: ---
            1 - Español
            2 - Inglés
            3 - Francés
            """;
System.out.println(menu);

try {
    var opcion = Integer.parseInt(teclado.nextLine());

    switch (opcion) {
        case 1: buscarLibroIdioma("es"); break;
        case 2: buscarLibroIdioma("en"); break;
        case 3: buscarLibroIdioma("fr"); break;
        default: System.out.println("Debe elegir una opción válida."); break;
    }
} catch (NumberFormatException e) {
    System.out.println("Opción no válida: " + e.getMessage());
}
}

private void buscarLibroIdioma(String idioma) {
    try {
        Idioma idiomaEnum = Idioma.valueOf(idioma.toUpperCase());
        List<Libro> libros = repository.buscarLibroIdioma(idiomaEnum);
        if (libros.isEmpty()) {
            System.out.println("En ese idioma no existen libros registrados.");
        } else {
            System.out.println();
            libros.forEach(l -> System.out.println(
                            "\nTítulo: " + l.getTitulo() +
                            "\nAutor: " + l.getAutor().getNombre() +
                            "\nIdioma: " + l.getIdioma().getIdioma() +
                            "\nNúmero de descargas: " + l.getDescargas() + "\n"
            ));
        }
    } catch (IllegalArgumentException e) {
        System.out.println("Indique una opción válida.");
    }
}


}

package com.alura.literalura.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;


/**
 * Clase Datos con respuesta de la API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Datos(
        @JsonAlias("count") Integer total,
        @JsonAlias("results") List<DatosLibro> libros) {
}

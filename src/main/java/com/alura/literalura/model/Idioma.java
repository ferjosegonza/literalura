package com.alura.literalura.model;

public enum Idioma {
    ES,
    EN,
    FR;

    public static Idioma fromString(String text) {
        for (Idioma idioma : Idioma.values()) {
            if (idioma.name().equalsIgnoreCase(text)) {
                return idioma;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}

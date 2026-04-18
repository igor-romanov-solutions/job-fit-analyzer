package io.github.igorromanovsolutions.jobfitanalyzer.domain;

import lombok.Getter;

@Getter
public enum GermanLevel {
    NOT_SPECIFIED(0),
    B1(1),
    B2(2),
    C1(3),
    C2(4),
    NATIVE(5),
    UNKNOWN(6);

    private final int priority;

    GermanLevel(int priority) {
        this.priority = priority;
    }

    public static GermanLevel from(String value) {
        if (value == null || value.isBlank()) {
            return NOT_SPECIFIED;
        }

        return switch (value.trim().toUpperCase()) {
            case "NOT SPECIFIED" -> NOT_SPECIFIED;
            case "B1" -> B1;
            case "B2" -> B2;
            case "C1" -> C1;
            case "C2" -> C2;
            case "NATIVE" -> NATIVE;
            default -> UNKNOWN;
        };
    }
}

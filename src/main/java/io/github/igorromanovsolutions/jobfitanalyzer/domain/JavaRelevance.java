package io.github.igorromanovsolutions.jobfitanalyzer.domain;

import lombok.Getter;

@Getter
public enum JavaRelevance {
    HIGH(0),
    MEDIUM(1),
    LOW(2),
    UNKNOWN(3);

    private final int priority;

    JavaRelevance(int priority) {
        this.priority = priority;
    }

    public static JavaRelevance from(String value) {
        if (value == null) {
            return UNKNOWN;
        }

        return switch (value.trim().toUpperCase()) {
            case "HIGH" -> HIGH;
            case "MEDIUM" -> MEDIUM;
            case "LOW" -> LOW;
            default -> UNKNOWN;
        };
    }
}

package me.romanov.jobfitanalyzer.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaRelevanceTest {

    @Test
    void shouldMapHigh() {
        assertEquals(JavaRelevance.HIGH, JavaRelevance.from("HIGH"));
        assertEquals(JavaRelevance.HIGH, JavaRelevance.from("high"));
        assertEquals(JavaRelevance.HIGH, JavaRelevance.from(" High "));
    }

    @Test
    void shouldMapMedium() {
        assertEquals(JavaRelevance.MEDIUM, JavaRelevance.from("MEDIUM"));
        assertEquals(JavaRelevance.MEDIUM, JavaRelevance.from("medium"));
    }

    @Test
    void shouldMapLow() {
        assertEquals(JavaRelevance.LOW, JavaRelevance.from("LOW"));
        assertEquals(JavaRelevance.LOW, JavaRelevance.from("low"));
    }

    @Test
    void shouldMapUnknownForNullOrUnexpectedValue() {
        assertEquals(JavaRelevance.UNKNOWN, JavaRelevance.from(null));
        assertEquals(JavaRelevance.UNKNOWN, JavaRelevance.from(""));
        assertEquals(JavaRelevance.UNKNOWN, JavaRelevance.from("something else"));
    }
}
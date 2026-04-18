package io.github.igorromanovsolutions.jobfitanalyzer.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GermanLevelTest {

    @Test
    void shouldMapNotSpecifiedForNullOrBlank() {
        assertEquals(GermanLevel.NOT_SPECIFIED, GermanLevel.from(null));
        assertEquals(GermanLevel.NOT_SPECIFIED, GermanLevel.from(""));
        assertEquals(GermanLevel.NOT_SPECIFIED, GermanLevel.from("   "));
    }

    @Test
    void shouldMapBLevels() {
        assertEquals(GermanLevel.B1, GermanLevel.from("B1"));
        assertEquals(GermanLevel.B1, GermanLevel.from("b1"));

        assertEquals(GermanLevel.B2, GermanLevel.from("B2"));
        assertEquals(GermanLevel.B2, GermanLevel.from("b2"));
    }

    @Test
    void shouldMapCLevels() {
        assertEquals(GermanLevel.C1, GermanLevel.from("C1"));
        assertEquals(GermanLevel.C2, GermanLevel.from("C2"));
    }

    @Test
    void shouldMapNative() {
        assertEquals(GermanLevel.NATIVE, GermanLevel.from("Native"));
        assertEquals(GermanLevel.NATIVE, GermanLevel.from("native"));
    }

    @Test
    void shouldMapUnknownForUnexpectedValue() {
        assertEquals(GermanLevel.UNKNOWN, GermanLevel.from("A2"));
        assertEquals(GermanLevel.UNKNOWN, GermanLevel.from("something else"));
    }
}
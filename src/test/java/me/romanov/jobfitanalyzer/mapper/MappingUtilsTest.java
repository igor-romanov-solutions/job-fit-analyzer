package me.romanov.jobfitanalyzer.mapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MappingUtilsTest {

    @Nested
    class JoinTests {
        @Test
        void shouldReturnNull_whenListIsNull() {
            assertNull(MappingUtils.join(null));
        }

        @Test
        void shouldReturnEmpty_whenListIsEmpty() {
            assertEquals("", MappingUtils.join(List.of()));
        }

        @Test
        void shouldReturnSameValue_whenListHasOneItem() {
            assertEquals("Item1", MappingUtils.join(List.of("Item1")));
        }

        @Test
        void shouldJoinItems_whenListHasTwoItems() {
            assertEquals("Item1, Item2", MappingUtils.join(List.of("Item1", "Item2")));
        }

        @Test
        void shouldJoinItems_whenListHasManyItems() {
            assertEquals("Item1, Item2, Item3", MappingUtils.join(List.of("Item1", "Item2", "Item3")));
        }
    }

    @Nested
    class SplitTests {
        @ParameterizedTest
        @MethodSource("splitCases")
        void shouldReturnExpectedList(String input, List<String> expected) {
            assertEquals(expected, MappingUtils.split(input));
        }

        static Stream<Arguments> splitCases() {
            return Stream.of(
                    Arguments.of(null, List.of()),
                    Arguments.of("", List.of()),
                    Arguments.of("one", List.of("one")),
                    Arguments.of("one, two", List.of("one", "two")),
                    Arguments.of("one, two, three", List.of("one", "two", "three")),
                    Arguments.of(", two, three", List.of("", "two", "three")),
                    Arguments.of("one,, three", List.of("one", "", "three")),
                    Arguments.of("one, two,", List.of("one", "two")),
                    Arguments.of(" one ,  two,three ", List.of("one", "two", "three"))
            );
        }
    }

    @Test
    void shouldPreserveStackThroughJoinAndSplit() {
        List<String> stack = List.of("Java", "Spring", "Kafka");

        String joined = MappingUtils.join(stack);
        List<String> split = MappingUtils.split(joined);

        assertEquals(stack, split);
    }
}
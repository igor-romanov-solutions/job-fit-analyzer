package me.romanov.jobfitanalyzer.mapper;

import java.util.Arrays;
import java.util.List;

public final class MappingUtils {

    private MappingUtils() {}

    public static String join(List<String> list) {
        return list == null ? null : String.join(", ", list);
    }

    public static String buildPreview(String text) {
        if (text == null) return null;
        int max = 300;
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    public static List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .toList();
    }
}

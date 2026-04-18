package io.github.igorromanovsolutions.jobfitanalyzer.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class OpenAiResponse {
    private List<Choice> choices;

    @Setter @Getter
    public static class Choice {
        private Message message;
    }

    @Getter @Setter
    public static class Message {
        private String content;

    }
}

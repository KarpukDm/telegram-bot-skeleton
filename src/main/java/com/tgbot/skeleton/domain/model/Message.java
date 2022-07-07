package com.tgbot.skeleton.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class Message {
    private final Content content;
    private Keyboard keyboard;
    // should be true when keyboard != null
    private boolean interactive;
    private boolean editSource;
    private boolean disableNotification;

    public Message(final Content content) {
        this.content = content;
        this.disableNotification = true;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Keyboard {
        private final List<List<String>> options;
        private final KeyboardType keyboardType;
        private final KeyboardOptionsType keyboardOptionsType;

        public Keyboard(final List<List<String>> options, final KeyboardType keyboardType) {
            this.options = options;
            this.keyboardType = keyboardType;
            this.keyboardOptionsType = KeyboardOptionsType.OPTION;
        }
    }

    @Getter
    @Builder
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private final String text;
        private String photoId;
        private String videoId;
        private Markdown markdown;

        @JsonCreator
        public Content(@JsonProperty("text") final String text,
                       @JsonProperty("photoId") final String photoId,
                       @JsonProperty("videoId") final String videoId) {
            this.text = text;
            this.photoId = photoId;
            this.videoId = videoId;
        }
    }
}

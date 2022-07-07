package com.tgbot.skeleton.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class TelegramContext {
    private final Sender sender;
    private final Message message;
    private final Location location;

    @Getter
    @RequiredArgsConstructor
    public static class Sender {
        private final String id;
        private final String chatId;
        private final String username;
        private final String firstName;
        private final String lastName;
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class Message {
        private final String id;
        private final String value;
        private String photoId;
        private String videoId;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Location {
        private final Double longitude;
        private final Double latitude;
    }
}

package com.tgbot.skeleton.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@RequiredArgsConstructor
public class User {
    private final String id;
    private final String chatId;
    private final String username;
    private final String firstName;
    private final String lastName;
}

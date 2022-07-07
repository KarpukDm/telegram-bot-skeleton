package com.tgbot.skeleton.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StoredMessage {
    private final String messageId;
    private final boolean interactive;
    private final boolean botOwns;
}

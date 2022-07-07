package com.tgbot.skeleton.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Markdown {
    MARKDOWN("MarkdownV2"),
    HTML("HTML");

    private final String value;
}

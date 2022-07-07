package com.tgbot.skeleton.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Result {
    private final CheckPoint nextCheckPoint;
    private final Message message;
    private final boolean clearHistory;
    // if you want to send some message to another user
    private final String destinationChatId;

    public Result(final CheckPoint nextCheckPoint) {
        this.nextCheckPoint = nextCheckPoint;
        this.message = null;
        this.clearHistory = false;
        this.destinationChatId = null;
    }

    public Result(final CheckPoint nextCheckPoint, final Message message, final boolean clearHistory) {
        this.nextCheckPoint = nextCheckPoint;
        this.message = message;
        this.clearHistory = clearHistory;
        this.destinationChatId = null;
    }
}

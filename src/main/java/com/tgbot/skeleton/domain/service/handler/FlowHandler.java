package com.tgbot.skeleton.domain.service.handler;


import com.tgbot.skeleton.domain.model.CheckPoint;
import com.tgbot.skeleton.domain.model.Message;
import com.tgbot.skeleton.domain.model.Result;
import com.tgbot.skeleton.domain.model.TelegramContext;
import com.tgbot.skeleton.domain.model.User;

import java.util.List;

public interface FlowHandler {

    // get current step
    CheckPoint getCheckPoint();

    // get greeting message
    Message greeting(User user);

    // handle user input
    Result handle(TelegramContext context, User user);

    // override if handler can handle tg / commands
    default String command() {
        return "";
    }

    // necessary only when using REPLY keyboard
    default List<String> allowedAnswers(User user) {
        return List.of();
    }
}

package com.tgbot.skeleton.domain.service.handler;

import com.tgbot.skeleton.domain.model.CheckPoint;
import com.tgbot.skeleton.domain.model.KeyboardType;
import com.tgbot.skeleton.domain.model.Message;
import com.tgbot.skeleton.domain.model.Result;
import com.tgbot.skeleton.domain.model.TelegramContext;
import com.tgbot.skeleton.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FirstStepHandler implements FlowHandler {
    @Override
    public CheckPoint getCheckPoint() {
        return CheckPoint.FIRST_STEP;
    }

    @Override
    public Message greeting(final User user) {
        return Message.builder()
                .content(new Message.Content(String.format("Hi %s", user.getFirstName())))
                .keyboard(new Message.Keyboard(List.of(List.of("Go to second step")), KeyboardType.AUTO))
                .build();
    }

    @Override
    public Result handle(final TelegramContext context, final User user) {
        if (!context.getMessage().getValue().equals("Go to second step")) {
            // show the error message and stay on the same step
            return new Result(getCheckPoint(), new Message(new Message.Content("Incorrect input")), false);
        }

        // redirect to next step
        return new Result(CheckPoint.SECOND_STEP);
    }
}

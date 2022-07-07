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
public class SecondStepHandler implements FlowHandler {
    @Override
    public CheckPoint getCheckPoint() {
        return CheckPoint.SECOND_STEP;
    }

    @Override
    public Message greeting(final User user) {
        return Message.builder()
                .content(new Message.Content("What do you want?"))
                .keyboard(new Message.Keyboard(List.of(List.of("Go back")), KeyboardType.AUTO))
                .build();
    }

    @Override
    public Result handle(final TelegramContext context, final User user) {
        if (!context.getMessage().getValue().equals("Go back")) {
            // show the error message and stay on the same step
            return new Result(getCheckPoint(), new Message(new Message.Content("Incorrect input")), false);
        }

        // redirect to next step
        return new Result(CheckPoint.FIRST_STEP);
    }
}

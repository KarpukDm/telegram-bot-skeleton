package com.tgbot.skeleton.domain;

import com.tgbot.skeleton.domain.model.CheckPoint;
import com.tgbot.skeleton.domain.model.Message;
import com.tgbot.skeleton.domain.model.RequestType;
import com.tgbot.skeleton.domain.model.Result;
import com.tgbot.skeleton.domain.model.StoredMessage;
import com.tgbot.skeleton.domain.model.TelegramContext;
import com.tgbot.skeleton.domain.model.User;
import com.tgbot.skeleton.domain.service.MessageService;
import com.tgbot.skeleton.domain.service.TelegramContextExtractor;
import com.tgbot.skeleton.domain.service.TelegramMessageSender;
import com.tgbot.skeleton.domain.service.UserService;
import com.tgbot.skeleton.domain.service.handler.FirstStepHandler;
import com.tgbot.skeleton.domain.service.handler.FlowHandler;
import com.tgbot.skeleton.utils.EnvironmentVariablesResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final TelegramContextExtractor telegramContextExtractor;
    private final TelegramMessageSender telegramMessageSender;
    private final UserService userService;
    private final MessageService messageService;

    private final List<FlowHandler> flowHandlers;

    @Override
    public void onUpdateReceived(final Update update) {
        final TelegramContext context = telegramContextExtractor.extract(update);
        log.info("User {} send message {} with value {}",
                context.getSender().getId(), context.getMessage().getId(), context.getMessage().getValue());

        User user = userService.findByUserId(context.getSender().getId());
        final RequestType requestType = checkRequest(context, user);
        if (requestType == RequestType.INVALID || requestType == RequestType.COMMAND) {
            return;
        }

        CheckPoint checkPoint = CheckPoint.FIRST_STEP;
        if (user == null) {
            log.info("Save new user {}", context.getSender().getId());
            user = userService.save(
                    context.getSender().getId(),
                    context.getSender().getChatId(),
                    context.getSender().getUsername(),
                    context.getSender().getFirstName(),
                    context.getSender().getLastName());
            userService.saveUsersCheckPoint(user.getId(), checkPoint);
        } else {
            checkPoint = userService.findUsersCheckPoint(user.getId());
            log.info("Start {}, user {}", checkPoint, user.getChatId());
            final FlowHandler handler = findHandler(checkPoint);
            final Result result = handler.handle(context, user);
            log.info("Finish {}, user {}", checkPoint, user.getChatId());
            userService.saveUsersCheckPoint(user.getId(), result.getNextCheckPoint());

            if (result.getMessage() != null) {
                if (result.getDestinationChatId() == null) {
                    // message is not null only if user should correct his answer
                    telegramMessageSender.send(context, result.getMessage(), getExecutors(user.getId(), result.getMessage().isInteractive()));
                    return;
                }

                final User target = userService.findByChatId(result.getDestinationChatId());
                if (target != null) {
                    telegramMessageSender.send(result.getDestinationChatId(), result.getMessage(), getExecutors(target.getId(), result.getMessage().isInteractive()));
                }
            }

            if (result.isClearHistory()) {
                clearHistory(user.getId(), user.getChatId());
            }

            checkPoint = result.getNextCheckPoint();
        }

        // prepare next step
        final FlowHandler handler = findHandler(checkPoint);
        final Message message = handler.greeting(user);
        telegramMessageSender.send(context, message, getExecutors(context.getSender().getId(), message.isInteractive()));
    }

    private RequestType checkRequest(final TelegramContext context, final User user) {
        final StoredMessage lastUserMessage = messageService.findLastUserMessage(context.getSender().getId());
        if (lastUserMessage == null || !lastUserMessage.isInteractive()) {
            if (user != null) {
                final Set<String> answers = flowHandlers.stream()
                        .flatMap(fh -> fh.allowedAnswers(user).stream())
                        .collect(Collectors.toSet());
                final Set<String> commands = flowHandlers.stream()
                        .map(FlowHandler::command)
                        .collect(Collectors.toSet());

                final CheckPoint cp = userService.findUsersCheckPoint(user.getId());
                final FlowHandler h = findHandler(cp);
                // удалить ответы из клавиатур, которые не удалились автоматически или случано нажатые команды
                // не обрабатывать дважды нажатые кнопки
                if (answers.contains(context.getMessage().getValue())
                        || commands.contains(context.getMessage().getValue())) {
                    deleteMessage(context.getSender().getChatId(), Integer.valueOf(context.getMessage().getId()));
                    return RequestType.INVALID;
                }
            }

            messageService.save(context.getSender().getId(), context.getMessage().getId(), false, false);
        } else if (!lastUserMessage.getMessageId().equals(context.getMessage().getId())) {
            final Optional<FlowHandler> handler = flowHandlers.stream()
                    .filter(h -> h.command().equals(context.getMessage().getValue()))
                    .findFirst();
            if (handler.isPresent()) {
                // не удаляем, если пользователь выбрал команду
                final Message message = handler.get().greeting(user);
                telegramMessageSender.send(context, message, getExecutors(context.getSender().getId(), message.isInteractive()));
                userService.saveUsersCheckPoint(user.getId(), handler.get().getCheckPoint());
                messageService.save(context.getSender().getId(), context.getMessage().getId(), false, false);
                return RequestType.COMMAND;
            }

            if (user != null) {
                final CheckPoint cp = userService.findUsersCheckPoint(user.getId());
                // не удаляем ответы с кейбоард маркапа
                // не удаляем расшаренную геопозицию
                final FlowHandler h = findHandler(cp);
                if (h.allowedAnswers(user).stream().anyMatch(a -> a.equals(context.getMessage().getValue()))) {
                    messageService.save(context.getSender().getId(), context.getMessage().getId(), false, false);
                } else {
                    deleteMessage(context.getSender().getChatId(), Integer.valueOf(context.getMessage().getId()));
                    return RequestType.INVALID;
                }
            }
        }

        return RequestType.VALID;
    }

    private FlowHandler findHandler(final CheckPoint checkPoint) {
        return flowHandlers.stream()
                .filter(h -> h.getCheckPoint() == checkPoint)
                .findFirst()
                .orElse(new FirstStepHandler());
    }

    private void clearHistory(final String userId, final String chatId) {
        final List<String> messageIds = messageService.findByUserId(userId);
        final List<String> success = new ArrayList<>();
        for (final String id : messageIds) {
            deleteMessage(chatId, Integer.valueOf(id));
            success.add(id);
        }
        messageService.deleteByIds(userId, success);
    }

    private void deleteMessage(final String chatId, final Integer messageId) {
        try {
            log.info("Delete message {} from chat {}", messageId, chatId);
            executeAsync(new DeleteMessage(chatId, messageId));
        } catch (TelegramApiException e) {
            log.error("Couldn't delete message {} for chat {}", messageId, chatId, e);
        }
    }

    private TelegramMessageSender.Executors getExecutors(final String userId, final boolean isInteractive) {
        final Consumer<SendMessage> sendMessageConsumer = s -> {
            try {
                final org.telegram.telegrambots.meta.api.objects.Message m = execute(s);
                messageService.save(userId, m.getMessageId().toString(), isInteractive, true);
            } catch (TelegramApiRequestException e) {
                if (e.getApiResponse() != null && e.getApiResponse().contains("bot was blocked by the user")) {
                    log.error("Bot was blocked by the user {}", userId, e);
                }
            } catch (TelegramApiException e) {
                log.error("Couldn't send message", e);
            }
        };

        final Consumer<SendPhoto> sendPhotoConsumer = s -> {
            try {
                final org.telegram.telegrambots.meta.api.objects.Message m = execute(s);
                messageService.save(userId, m.getMessageId().toString(), isInteractive, true);
            } catch (TelegramApiRequestException e) {
                if (e.getApiResponse() != null && e.getApiResponse().contains("bot was blocked by the user")) {
                    log.error("Bot was blocked by the user {}", userId, e);
                }
            } catch (TelegramApiException e) {
                log.error("Couldn't send photo", e);
            }
        };

        final Consumer<SendVideo> sendVideoConsumer = s -> {
            try {
                final org.telegram.telegrambots.meta.api.objects.Message m = execute(s);
                messageService.save(userId, m.getMessageId().toString(), isInteractive, true);
            } catch (TelegramApiRequestException e) {
                if (e.getApiResponse() != null && e.getApiResponse().contains("bot was blocked by the user")) {
                    log.error("Bot was blocked by the user {}", userId, e);
                }
            } catch (TelegramApiException e) {
                log.error("Couldn't send video", e);
            }
        };

        final Consumer<EditMessageText> editMessageConsumer = s -> {
            try {
                execute(s);
            } catch (TelegramApiRequestException e) {
                if (e.getApiResponse() != null && e.getApiResponse().contains("bot was blocked by the user")) {
                    log.error("Bot was blocked by the user {}", userId, e);
                }
            } catch (TelegramApiException e) {
                log.error("Couldn't edit message", e);
            }
        };

        return new TelegramMessageSender.Executors(sendMessageConsumer, sendPhotoConsumer, sendVideoConsumer, editMessageConsumer);
    }

    @Override
    public String getBotUsername() {
        return EnvironmentVariablesResolver.getAsString("TG_BOT_NAME");
    }

    @Override
    public String getBotToken() {
        return EnvironmentVariablesResolver.getAsString("TG_BOT_TOKEN");
    }
}

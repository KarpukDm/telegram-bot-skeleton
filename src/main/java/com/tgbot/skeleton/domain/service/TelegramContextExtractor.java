package com.tgbot.skeleton.domain.service;

import com.tgbot.skeleton.domain.model.TelegramContext;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.ChatMemberUpdated;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.tgbot.skeleton.utils.OptionalUtils.optionalOf;

@Service
public class TelegramContextExtractor {

    public TelegramContext extract(final Update update) {
        if (update.hasMessage()) {
            return extractFromMessage(update.getMessage());
        } else if (update.hasEditedMessage()) {
            return extractFromMessage(update.getEditedMessage());
        } else if (update.hasMyChatMember()) {
            return extractFromMyChatMember(update.getMyChatMember());
        }
        return extractFromCallback(update.getCallbackQuery());
    }

    private static TelegramContext extractFromMessage(final Message tgMessage) {
        final User from = tgMessage.getFrom();
        final TelegramContext.Sender sender = new TelegramContext.Sender(
                from.getId().toString(),
                tgMessage.getChatId().toString(),
                from.getUserName(),
                from.getFirstName(),
                from.getLastName());

        final TelegramContext.Message message = new TelegramContext.Message(
                tgMessage.getMessageId().toString(),
                tgMessage.getCaption() == null ? tgMessage.getText() : tgMessage.getCaption());

        message.setPhotoId(optionalOf(() -> tgMessage.getPhoto().get(0).getFileId()));
        message.setVideoId(optionalOf(() -> tgMessage.getVideo().getFileId()));

        return new TelegramContext(sender, message, extractLocation(tgMessage.getLocation()));
    }

    private static TelegramContext extractFromMyChatMember(final ChatMemberUpdated memberUpdated) {
        final User user = memberUpdated.getFrom();
        final Chat chat = memberUpdated.getChat();

        final TelegramContext.Sender sender = new TelegramContext.Sender(
                user.getId().toString(),
                chat.getId().toString(),
                user.getUserName(),
                user.getFirstName(),
                user.getLastName());

        return new TelegramContext(sender, null, null);
    }

    private static TelegramContext extractFromCallback(final CallbackQuery query) {
        final Message tgMessage = query.getMessage();
        final Chat chat = query.getMessage().getChat();

        final TelegramContext.Sender sender = new TelegramContext.Sender(
                chat.getId().toString(),
                tgMessage.getChatId().toString(),
                chat.getUserName(),
                chat.getFirstName(),
                chat.getLastName());

        final TelegramContext.Message message = new TelegramContext.Message(
                tgMessage.getMessageId().toString(),
                query.getData());

        return new TelegramContext(sender, message, extractLocation(query.getMessage().getLocation()));
    }

    private static TelegramContext.Location extractLocation(final Location location) {
        if (location == null) {
            return null;
        }
        return new TelegramContext.Location(location.getLongitude(), location.getLatitude());
    }
}

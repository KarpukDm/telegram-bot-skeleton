package com.tgbot.skeleton.domain.service;

import com.tgbot.skeleton.domain.model.KeyboardOptionsType;
import com.tgbot.skeleton.domain.model.KeyboardType;
import com.tgbot.skeleton.domain.model.Message;
import com.tgbot.skeleton.domain.model.TelegramContext;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class TelegramMessageSender {

    public void send(final TelegramContext context, final Message message, final Executors executors) {
        if (message == null) {
            return;
        }

        if (message.isEditSource()) {
            editMessage(context, message, executors.getEditMessage());
        } else {
            if (message.getContent().getPhotoId() != null) {
                sendPhoto(context.getSender().getChatId(), message, executors.getSendPhoto());
            } else if (message.getContent().getVideoId() != null) {
                sendVideo(context.getSender().getChatId(), message, executors.getSendVideo());
            } else {
                sendMessage(context.getSender().getChatId(), message, executors.getSendMessage());
            }
        }
    }

    public void send(final String chatId, final Message message, final Executors executors) {
        if (message == null) {
            return;
        }

        if (message.getContent().getPhotoId() != null) {
            sendPhoto(chatId, message, executors.getSendPhoto());
        } else if (message.getContent().getVideoId() != null) {
            sendVideo(chatId, message, executors.getSendVideo());
        } else {
            sendMessage(chatId, message, executors.getSendMessage());
        }
    }

    private static void sendMessage(final String chatId, final Message message, final Consumer<SendMessage> sendMessage) {
        final SendMessage msg = new SendMessage();
        msg.setChatId(chatId);
        msg.setText(message.getContent().getText());
        msg.setReplyMarkup(buildKeyboard(message));
        msg.setDisableNotification(message.isDisableNotification());

        if (message.getContent().getMarkdown() != null) {
            msg.setParseMode(message.getContent().getMarkdown().getValue());
        }

        sendMessage.accept(msg);
    }

    private static void sendPhoto(final String chatId, final Message message, final Consumer<SendPhoto> sendPhoto) {
        final SendPhoto photo = new SendPhoto();
        photo.setChatId(chatId);

        final InputFile inputFile = new InputFile();
        inputFile.setMedia(message.getContent().getPhotoId());
        photo.setPhoto(inputFile);
        photo.setCaption(message.getContent().getText());
        photo.setReplyMarkup(buildKeyboard(message));
        photo.setDisableNotification(message.isDisableNotification());

        sendPhoto.accept(photo);
    }

    private static void sendVideo(final String chatId, final Message message, final Consumer<SendVideo> sendVideo) {
        final SendVideo video = new SendVideo();
        video.setChatId(chatId);

        final InputFile inputFile = new InputFile();
        inputFile.setMedia(message.getContent().getVideoId());
        video.setVideo(inputFile);
        video.setCaption(message.getContent().getText());
        video.setReplyMarkup(buildKeyboard(message));
        video.setDisableNotification(message.isDisableNotification());

        sendVideo.accept(video);
    }

    private static void editMessage(final TelegramContext context, final Message message, final Consumer<EditMessageText> editMessage) {
        final EditMessageText edit = new EditMessageText();
        edit.setChatId(context.getSender().getChatId());
        edit.setMessageId(Integer.valueOf(context.getMessage().getId()));
        edit.setText(message.getContent().getText());
        edit.setReplyMarkup(buildInlineKeyboard(message.getKeyboard()));

        if (message.getContent().getMarkdown() != null) {
            edit.setParseMode(message.getContent().getMarkdown().getValue());
        }

        editMessage.accept(edit);
    }

    private static ReplyKeyboard buildKeyboard(final Message message) {
        if (message.getKeyboard() == null) {
            return null;
        }

        if (message.getKeyboard().getKeyboardType() == KeyboardType.AUTO) {
            if (message.getContent().getPhotoId() != null || message.getContent().getVideoId() != null) {
                return buildReplyKeyboard(message.getKeyboard());
            }

            return buildInlineKeyboard(message.getKeyboard());
        }

        if (message.getKeyboard().getKeyboardType() == KeyboardType.REPLY) {
            return buildReplyKeyboard(message.getKeyboard());
        }

        return buildInlineKeyboard(message.getKeyboard());
    }

    private static InlineKeyboardMarkup buildInlineKeyboard(final Message.Keyboard mKeyboard) {
        final List<List<String>> options = mKeyboard.getOptions();
        if (CollectionUtils.isEmpty(options)) {
            return null;
        }
        final List<List<InlineKeyboardButton>> rows = options.stream()
                .map(os -> os.stream()
                        .map(o -> {
                            final InlineKeyboardButton button = new InlineKeyboardButton();
                            button.setText(o);
                            button.setCallbackData(o);
                            return button;
                        }).collect(Collectors.toList()))
                .collect(Collectors.toList());

        final InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);

        return keyboard;
    }

    public static ReplyKeyboard buildReplyKeyboard(final Message.Keyboard mKeyboard) {
        final List<List<String>> options = mKeyboard.getOptions();
        if (CollectionUtils.isEmpty(options)) {
            return new ReplyKeyboardRemove(true);
        }

        final ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        final List<KeyboardRow> rows = options.stream()
                .map(os -> {
                    final List<KeyboardButton> buttons = os.stream()
                            .map(o -> {
                                final KeyboardButton button = new KeyboardButton(o);
                                if (mKeyboard.getKeyboardOptionsType() == KeyboardOptionsType.REQUEST_LOCATION) {
                                    button.setRequestLocation(true);
                                }
                                if (mKeyboard.getKeyboardOptionsType() == KeyboardOptionsType.REQUEST_CONTACT) {
                                    button.setRequestContact(true);
                                }
                                return button;
                            })
                            .collect(Collectors.toList());
                    return new KeyboardRow(buttons);
                }).collect(Collectors.toList());

        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);

        return keyboard;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Executors {
        private final Consumer<SendMessage> sendMessage;
        private final Consumer<SendPhoto> sendPhoto;
        private final Consumer<SendVideo> sendVideo;
        private final Consumer<EditMessageText> editMessage;
    }
}

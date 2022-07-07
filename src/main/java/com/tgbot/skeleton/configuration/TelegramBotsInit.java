package com.tgbot.skeleton.configuration;

import com.tgbot.skeleton.domain.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@RequiredArgsConstructor
public class TelegramBotsInit {

    private final TelegramBot telegramBot;

    @Bean
    public TelegramBotsApi init() {
        try {
            final TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(telegramBot);

            return telegramBotsApi;
        } catch (TelegramApiException e) {
            throw new IllegalStateException(e);
        }
    }
}

package com.example.demo;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfig {

    private final BotService botService;

    public BotConfig(BotService botService) {
        this.botService = botService;
    }

    @Bean
    @SneakyThrows
    public TelegramBot telegramBot(
            @Value("7632623657:AAHO7S8MYg4kQVMXVUkdjXL6mK4_0hi7N6E") String botToken,
            TelegramBotsApi telegramBotsApi
    ) {
        var botOptions = new DefaultBotOptions();
        var bot = new TelegramBot(botOptions, botToken, botService);
        telegramBotsApi.registerBot(bot);
        return bot;
    }

    @Bean
    @SneakyThrows
    public TelegramBotsApi telegramBotsApi() {
        return new TelegramBotsApi(DefaultBotSession.class);
    }
}

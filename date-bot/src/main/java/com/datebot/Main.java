package com.datebot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

    public static void main(String[] args) {

        // Читаем токен и username из переменных окружения (для Railway/VPS)
        // Если переменная не задана — берём значение по умолчанию из кода
        String botToken    = System.getenv("BOT_TOKEN");
        String botUsername = System.getenv("BOT_USERNAME");

        // Fallback: если запускаешь локально без переменных окружения —
        // замени строки ниже на свои данные
        if (botToken == null || botToken.isBlank()) {
            botToken = "ВАШ_ТОКЕН_ЗДЕСЬ"; // ← только для локального запуска
        }
        if (botUsername == null || botUsername.isBlank()) {
            botUsername = "ВАШ_USERNAME_БОТА_ЗДЕСЬ"; // ← только для локального запуска
        }

        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(new DateBot(botToken, botUsername));
            System.out.println("✅ Бот запущен: @" + botUsername);
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка запуска: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

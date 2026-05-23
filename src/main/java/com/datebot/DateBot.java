package com.datebot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

/**
 * Бот-приглашение на свидание.
 * Использует InlineKeyboard (кнопки прямо под сообщением).
 */
public class DateBot extends TelegramLongPollingBot {

    private final String botUsername;

    // Callback-данные для кнопок
    private static final String CB_YES      = "yes";
    private static final String CB_NO       = "no";
    private static final String CB_MFC      = "mfc";
    private static final String CB_TAX      = "tax";
    private static final String CB_BERLOGA  = "berloga";

    public DateBot(String botToken, String botUsername) {
        super(botToken);
        this.botUsername = botUsername;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    // ─── Входящие сообщения (текст / команды) ─────────────────────────────────

    @Override
    public void onUpdateReceived(Update update) {

        // Нажатие на inline-кнопку
        if (update.hasCallbackQuery()) {
            handleCallback(update);
            return;
        }

        // Текстовое сообщение или команда /start
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text  = update.getMessage().getText().trim();
            long   chatId = update.getMessage().getChatId();

            if (text.equals("/start")) {
                sendDateInvite(chatId);
            } else {
                // На любой другой текст — показываем приглашение снова
                sendDateInvite(chatId);
            }
        }
    }

    // ─── Первое сообщение-приглашение ─────────────────────────────────────────

    private void sendDateInvite(long chatId) {
        String text = "💌 Привет, моя хорошая!\n\n"
                + "Я долго думал и очень хочу спросить...\n\n"
                + "*Ты пойдёшь со мной на свидание?* 🥺";

        InlineKeyboardMarkup keyboard = buildKeyboard(
                btn("❤️ Да!", CB_YES),
                btn("Нет",    CB_NO)
        );

        sendMessage(chatId, text, keyboard);
    }

    // ─── Обработка нажатий на кнопки ──────────────────────────────────────────

    private void handleCallback(Update update) {
        var query    = update.getCallbackQuery();
        String data  = query.getData();
        long chatId  = query.getMessage().getChatId();
        int  msgId   = query.getMessage().getMessageId();

        switch (data) {

            case CB_NO -> {
                // Кнопка «Нет» — «не работает», ничего не меняем, просто отвечаем всплывашкой
                answerCallback(query.getId(), "⚠️ Эта кнопка не работает 😏");
            }

            case CB_YES -> {
                // Заменяем сообщение — просим выбрать место
                String text = "🥳 Ура! Я так рад!\n\n"
                        + "Тогда выбирай место для нашего свидания 👇";

                InlineKeyboardMarkup keyboard = buildKeyboard(
                        btn("🏛 МФЦ",      CB_MFC),
                        btn("📋 Налоговая", CB_TAX),
                        btn("🐻 Берлога",   CB_BERLOGA)
                );

                editMessage(chatId, msgId, text, keyboard);
                answerCallback(query.getId(), null);
            }

            case CB_MFC -> {
                String text = "🏛 МФЦ? Серьёзно??\n\n"
                        + "Ладно... возьму талончик, буду держать твою руку в очереди 😄\n"
                        + "Главное — ты рядом! ❤️";
                editMessage(chatId, msgId, text, null);
                answerCallback(query.getId(), null);
            }

            case CB_TAX -> {
                String text = "📋 Налоговая... романтично!\n\n"
                        + "Заполним декларацию вместе, я буду смотреть на тебя "
                        + "а не на бумажки 😍\n"
                        + "Ты делаешь любое место особенным! ❤️";
                editMessage(chatId, msgId, text, null);
                answerCallback(query.getId(), null);
            }

            case CB_BERLOGA -> {
                String text = "🐻 Берлога...\n\n"
                        + "Нуууу сеееекс 😏🔥\n\n"
                        + "❤️";
                editMessage(chatId, msgId, text, null);
                answerCallback(query.getId(), null);
            }
        }
    }

    // ─── Вспомогательные методы ───────────────────────────────────────────────

    /** Отправляет новое сообщение с inline-клавиатурой (или без неё). */
    private void sendMessage(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText(text);
        msg.setParseMode("Markdown");
        if (keyboard != null) msg.setReplyMarkup(keyboard);

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки: " + e.getMessage());
        }
    }

    /** Редактирует уже отправленное сообщение (меняет текст и кнопки). */
    private void editMessage(long chatId, int messageId, String text, InlineKeyboardMarkup keyboard) {
        EditMessageText edit = new EditMessageText();
        edit.setChatId(String.valueOf(chatId));
        edit.setMessageId(messageId);
        edit.setText(text);
        edit.setParseMode("Markdown");
        if (keyboard != null) edit.setReplyMarkup(keyboard);

        try {
            execute(edit);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка редактирования: " + e.getMessage());
        }
    }

    /** Отвечает на callback-запрос (убирает «часики» у кнопки). */
    private void answerCallback(String callbackId, String notification) {
        var answer = new org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackId);
        if (notification != null) {
            answer.setText(notification);
            answer.setShowAlert(true); // всплывающий алерт
        }
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка callback: " + e.getMessage());
        }
    }

    /** Создаёт кнопку с текстом и callback-данными. */
    private InlineKeyboardButton btn(String label, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(label);
        button.setCallbackData(callbackData);
        return button;
    }

    /**
     * Собирает клавиатуру из кнопок.
     * Каждая кнопка — на своей строке.
     */
    private InlineKeyboardMarkup buildKeyboard(InlineKeyboardButton... buttons) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (InlineKeyboardButton btn : buttons) {
            rows.add(List.of(btn));
        }
        markup.setKeyboard(rows);
        return markup;
    }
}

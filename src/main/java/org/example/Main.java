package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new PartyBot());
            System.out.println("🤖 Бот успішно запущений!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

class PartyBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        return "tusa_jusas_bot";
    }

    @Override
    public String getBotToken() {
        return "8951171697:AAETY1OCVkwMfh2POyCuNkqIJFFKzU47Mvw";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (text.equals("/start")) {
                sendMiniAppButton(chatId);
            }
        }
    }

    private void sendMiniAppButton(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Вітаємо у тусовелі Львова! 🍸\nНатисни кнопку нижче, щоб відкрити каталог закладів:");

        InlineKeyboardButton webAppBtn = new InlineKeyboardButton();
        webAppBtn.setText("🎉 Відкрити Каталог Закладів");

        // ВСТАВТЕ СЮДЕ СВОЄ ПОСИЛАННЯ З GITHUB PAGES замість старого:
        webAppBtn.setWebApp(new WebAppInfo("https://skrypkavanek-netizen.github.io/party-in-lviv/"));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(webAppBtn));
        markup.setKeyboard(rows);

        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
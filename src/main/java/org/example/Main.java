package org.example;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Запускаємо Telegram-бота ( PartyBot )
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new PartyBot());
            System.out.println("🤖 Бот успішно запущений!");

            // 2. Піднімаємо HTTP-сервер для Render, щоб він бачив відкритий порт
            startHttpServer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startHttpServer() {
        try {
            String portStr = System.getenv("PORT");
            int port = (portStr != null && !portStr.isEmpty()) ? Integer.parseInt(portStr) : 8080;

            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            
            server.createContext("/", exchange -> {
                String response = "Bot is running 24/7!";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            });

            server.setExecutor(null);
            server.start();
            System.out.println("🌐 HTTP сервер успішно запущено на порті: " + port);

        } catch (IOException e) {
            System.err.println("Помилка запуску HTTP сервера: " + e.getMessage());
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

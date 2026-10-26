package org.example;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;

public class Main {
    private static final String BOT_TOKEN = "8951171697:AAETY1OCVkwMfh2POyCuNkqIJFFKzU47Mvw";
    private static final String RENDER_URL = "https://lviv-party-bot.onrender.com";

    public static void main(String[] args) {
        try {
            // 1. Запускаємо HTTP-сервер для прийому запитів від Render та Telegram
            startHttpServer();

            // 2. Автоматично реєструємо Webhook у Telegram
            registerWebhook();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startHttpServer() throws IOException {
        String portStr = System.getenv("PORT");
        int port = (portStr != null && !portStr.isEmpty()) ? Integer.parseInt(portStr) : 10000;

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                // Telegram надсилає оновлення (повідомлення від користувачів) сюди
                try {
                    byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
                    ObjectMapper mapper = new ObjectMapper();
                    Update update = mapper.readValue(bodyBytes, Update.class);
                    
                    // Обробляємо повідомлення
                    handleUpdate(update);

                    String response = "OK";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, 0);
                    exchange.close();
                }
            } else {
                // GET-запит для перевірки статусу (щоб Render бачив, що сервіс живий)
                String response = "Bot is running 24/7 via Webhook!";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("🌐 HTTP сервер успішно запущено на порті: " + port);
    }

    private static void registerWebhook() {
        try {
            String url = "https://api.telegram.org/bot" + BOT_TOKEN + "/setWebhook?url=" + RENDER_URL;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("🔗 Реєстрація Webhook у Telegram: " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleUpdate(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            System.out.println("Отримано текст: " + text + " від chatId: " + chatId);

            if (text.equals("/start")) {
                sendMiniAppButton(chatId);
            }
        }
    }

    private static void sendMiniAppButton(long chatId) {
        try {
            // Формуємо JSON для відправки повідомлення з WebApp кнопкою через офіційне API Telegram
            String jsonBody = "{" +
                    "\"chat_id\":\"" + chatId + "\"," +
                    "\"text\":\"Вітаємо у тусовелі Львова! 🍸\\nНатисни кнопку нижче, щоб відкрити каталог закладів:\"," +
                    "\"reply_markup\":{" +
                        "\"inline_keyboard\":[[{" +
                            "\"text\":\"🎉 Відкрити Каталог Закладів\"," +
                            "\"web_app\":{\"url\":\"https://skrypkavanek-netizen.github.io/party-in-lviv/\"}" +
                        "}]]" +
                    "}" +
                "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            
            client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Відповідь на /start успішно надіслана!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

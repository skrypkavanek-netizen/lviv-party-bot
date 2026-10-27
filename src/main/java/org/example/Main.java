package org.example;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final String BOT_TOKEN = System.getenv("BOT_TOKEN");
private static final String RENDER_URL = System.getenv("RENDER_URL");
    public static void main(String[] args) {
        try {
            startHttpServer();
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
            String method = exchange.getRequestMethod();
            
            // Якщо це GET-запит (наприклад, від UptimeRobot або браузера)
            if ("GET".equalsIgnoreCase(method)) {
                String response = "Lviv Party Bot is active and running 24/7! 🍸";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }

            // Якщо це POST-запит від Telegram
            if ("POST".equalsIgnoreCase(method)) {
                try {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    
                    // Безпечний парсинг з перевіркою на null
                    String chatId = extractJsonField(requestBody, "chat", "id");
                    String text = extractStringField(requestBody, "text");

                    // Якщо текст відсутній (наприклад, користувач надіслав стікер, фото чи натиснув кнопку)
                    if (text == null) {
                        text = "";
                    }

                    System.out.println("Отримано запит. Текст: '" + text + "' від chatId: " + chatId);

                    if ("/start".equals(text) && chatId != null) {
                        sendMiniAppButton(chatId);
                    }

                    String response = "OK";
                    exchange.sendResponseHeaders(200, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    System.err.println("Помилка при обробці повідомлення від Telegram: " + e.getMessage());
                    String response = "Internal Error";
                    exchange.sendResponseHeaders(500, response.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(405, 0); // Method Not Allowed
                exchange.close();
            }
        });
        server.setExecutor(null);
        server.start();
        System.out.println("🌐 HTTP сервер успішно запущено на порті: " + port);
    }

    private static String extractStringField(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String extractJsonField(String json, String parentField, String field) {
        Pattern pattern = Pattern.compile("\"" + parentField + "\"\\s*:\\\\?s*\\{[^}]*\"" + field + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Запасний варіант для простого пошуку ID
        Pattern patternSimple = Pattern.compile("\"" + field + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcherSimple = patternSimple.matcher(json);
        if (matcherSimple.find()) {
            return matcherSimple.group(1);
        }
        return null;
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

    private static void sendMiniAppButton(String chatId) {
        try {
            // Динамічний параметр часу збірки/запуску захищає від застрягання в кеші Telegram
            long cacheVersion = System.currentTimeMillis();
            String webAppUrl = "https://skrypkavanek-netizen.github.io/lviv-party-bot/?v=" + cacheVersion;

            String jsonBody = "{" +
                    "\"chat_id\":\"" + chatId + "\"," +
                    "\"text\":\"Вітаємо у тусовелі Львова! 🍸\\nНатисни кнопку нижче, щоб відкрити каталог закладів:\"," +
                    "\"reply_markup\":{" +
                        "\"inline_keyboard\":[[{" +
                            "\"text\":\"🎉 Відкрити Каталог Закладів\"," +
                            "\"web_app\":{\"url\":\"" + webAppUrl + "\"}" +
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
            System.out.println("Відповідь на /start успішно надіслана з версією сайту: " + cacheVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

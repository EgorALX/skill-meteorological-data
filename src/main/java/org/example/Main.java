package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.*;
import java.net.URI;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        double lat = 55.75;
        double lon = 37.62;
        int limit = 5;

        String apiKey = System.getenv("YANDEX_WEATHER_KEY");
        String url = "https://api.weather.yandex.ru/v2/forecast?lat=" + lat + "&lon=" + lon + "&limit=" + limit;

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("Не задана переменная окружения YANDEX_WEATHER_KEY");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("X-Yandex-Weather-Key", apiKey)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("HTTP статус: " + response.statusCode());

        String body = response.body();
        System.out.println(body);

        if (response.statusCode() != 200) {
            return;
        }

        JsonNode root = new ObjectMapper().readTree(body);

        int currentTemp = root.path("fact").path("temp").asInt();
        System.out.println("Текущая температура: " + currentTemp);

        JsonNode forecasts = root.path("forecasts");

        int days = Math.min(limit, forecasts.size());
        double sum = 0;
        int count = 0;

        for (int i = 0; i < days; i++) {
            JsonNode tempAvg = forecasts.get(i).path("parts").path("day").path("temp_avg");
            if (tempAvg.isNumber()) {
                sum += tempAvg.asDouble();
                count++;
            }
        }

        if (count > 0) {
            System.out.println("Средняя дневная температура за " + count + " сут : " + (sum / count));
        } else {
            System.out.println("Нет данных temp_avg в прогнозе");
        }
    }
}

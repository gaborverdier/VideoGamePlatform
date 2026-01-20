package org.example.services;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class PlatformApiClient {

    private final String baseUrl; // ex: http://localhost:8082
    private final HttpClient http;

    public PlatformApiClient(String baseUrl) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.http = HttpClient.newHttpClient();
    }

    // GET /api/games
    public String getAllGamesJson() throws Exception {
        return get("/api/games");
    }

    // GET /api/games/{gameId}
    public String getGameByIdJson(String gameId) throws Exception {
        return get("/api/games/" + urlPath(gameId));
    }

    // GET /api/games/search?title=...
    public String searchGamesByTitleJson(String title) throws Exception {
        return get("/api/games/search?title=" + urlQuery(title));
    }

    // GET /api/games/genre/{genre}
    public String getGamesByGenreJson(String genre) throws Exception {
        return get("/api/games/genre/" + urlPath(genre));
    }

    // GET /api/games/platform/{platform}
    public String getGamesByPlatformJson(String platform) throws Exception {
        return get("/api/games/platform/" + urlPath(platform));
    }

    private String get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        throw new RuntimeException("GET " + path + " failed: status=" + response.statusCode() + " body=" + response.body());
    }

    //sert à encoder les paramètres dans l’URL  par exemple: /search?title=FIFA%202024
    private static String urlQuery(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
    //sert à encoder les segments de chemin dans l’URL  par exemple: /genre/Action%20RPG
    private static String urlPath(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
    //supprime la barre oblique finale d’une URL si elle existe
    private static String trimTrailingSlash(String s) {
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}

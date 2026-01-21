package org.example.services;

import org.example.util.ApiClient;

public class PlatformApiClient {
    public PlatformApiClient() {
        // Optionally set base URL here if needed
    }

    // GET /api/games
    public String getAllGamesJson() throws Exception {
        return ApiClient.get("/api/games");
    }

    // GET /api/games/{gameId}
    public String getGameByIdJson(String gameId) throws Exception {
        return ApiClient.get("/api/games/" + urlPath(gameId));
    }

    // GET /api/games/search?title=...
    public String searchGamesByTitleJson(String title) throws Exception {
        return ApiClient.get("/api/games/search?title=" + urlQuery(title));
    }

    // GET /api/games/genre/{genre}
    public String getGamesByGenreJson(String genre) throws Exception {
        return ApiClient.get("/api/games/genre/" + urlPath(genre));
    }

    // GET /api/games/platform/{platform}
    public String getGamesByPlatformJson(String platform) throws Exception {
        return ApiClient.get("/api/games/platform/" + urlPath(platform));
    }

    // Utility methods for encoding
    private static String urlQuery(String s) {
        return java.net.URLEncoder.encode(s == null ? "" : s, java.nio.charset.StandardCharsets.UTF_8);
    }
    private static String urlPath(String s) {
        return java.net.URLEncoder.encode(s == null ? "" : s, java.nio.charset.StandardCharsets.UTF_8);
    }
}

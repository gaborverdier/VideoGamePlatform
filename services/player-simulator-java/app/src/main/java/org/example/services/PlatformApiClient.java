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

    // GET /api/library/user/{userId}
    public String getUserLibraryJson(String userId) throws Exception {
        return ApiClient.get("/api/library/user/" + urlPath(userId));
    }

    // PUT /api/library/user/{userId}/game/{gameId}/install
    public void installGame(String userId, String gameId) throws Exception {
        ApiClient.put("/api/library/user/" + urlPath(userId) + "/game/" + urlPath(gameId) + "/install");
    }

    // POST /api/library/user/{userId}/game/{gameId}/purchase
    public void purchaseGame(String userId, String gameId) throws Exception {
        ApiClient.postJson("/api/library/user/" + urlPath(userId) + "/game/" + urlPath(gameId) + "/purchase", "{}");
    }

    // GET /api/notifications/user/{userId}
    public String getUserNotificationsJson(String userId) throws Exception {
        return ApiClient.get("/api/notifications/user/" + urlPath(userId));
    }

    // GET /api/reviews/game/{gameId}
    public String getReviewsForGameJson(String gameId) throws Exception {
        return ApiClient.get("/api/reviews/game/" + urlPath(gameId));
    }

    // GET /api/wishlist/user/{userId}
    public String getUserWishlistJson(String userId) throws Exception {
        return ApiClient.get("/api/wishlist/user/" + urlPath(userId));
    }

    // POST /api/wishlist/new
    public String addToWishlistJson(String jsonBody) throws Exception {
        return ApiClient.postJson("/api/wishlist/new", jsonBody);
    }

    // DELETE /api/wishlist/user/{userId}/game/{gameId}
    public String deleteUserWishlistEntry(String userId, String gameId) throws Exception {
        return ApiClient.delete("/api/wishlist/user/" + urlPath(userId) + "/game/" + urlPath(gameId));
    }
}

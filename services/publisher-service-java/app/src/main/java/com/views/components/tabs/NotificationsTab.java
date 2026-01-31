package com.views.components.tabs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.model.CrashAggregation;
import com.model.Review;
import com.util.ApiClient;
import com.util.AvroJacksonConfig;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NotificationsTab extends ScrollPane {

    private VBox notificationsList;
    private List<CrashAggregation> crashReports;
    private List<Review> reviews;
    private Map<String, String> gameIdToName = new HashMap<>();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public NotificationsTab() {
        this.crashReports = new ArrayList<>();
        this.reviews = new ArrayList<>();

        notificationsList = new VBox(10);
        notificationsList.setPadding(new Insets(20));
        notificationsList.setStyle("-fx-background-color: #2b2b2b;");
        
        updateView();

        this.setContent(notificationsList);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: #2b2b2b;");
    }

    public List<Review> getReviews() {
        return reviews;
    }

    private void updateView() {
        System.out.println("[NotificationsTab] updateView() called - crashReports.size() = " + crashReports.size() + ", reviews.size() = " + reviews.size());
        notificationsList.getChildren().clear();

        Label titleLabel = new Label("Notifications");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        notificationsList.getChildren().add(titleLabel);

        // Section Crash Reports
        if (!crashReports.isEmpty()) {
            // Limiter à 10 derniers crashs
            List<CrashAggregation> recentCrashes = crashReports.stream()
                .limit(10)
                .collect(Collectors.toList());
            
            Label crashTitle = new Label("⚠️ RAPPORTS DE CRASH (" + recentCrashes.size() + ")");
            crashTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FF6B6B;");
            notificationsList.getChildren().add(crashTitle);

            for (CrashAggregation crash : recentCrashes) {
                VBox crashCard = createCrashReportCard(crash);
                notificationsList.getChildren().add(crashCard);
            }

            notificationsList.getChildren().add(new Separator());
        }

        // Section Reviews
        if (!reviews.isEmpty()) {
            Label reviewTitle = new Label("⭐ AVIS DES JOUEURS (" + reviews.size() + ")");
            reviewTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");
            notificationsList.getChildren().add(reviewTitle);

            // Trier par date décroissante
            List<Review> sortedReviews = reviews.stream()
                .sorted((r1, r2) -> r2.getTimestamp().compareTo(r1.getTimestamp()))
                .collect(Collectors.toList());

            for (Review review : sortedReviews) {
                VBox reviewCard = createReviewCard(review);
                notificationsList.getChildren().add(reviewCard);
            }
        }

        if (crashReports.isEmpty() && reviews.isEmpty()) {
            Label emptyLabel = new Label("Aucune notification pour le moment");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaa;");
            notificationsList.getChildren().add(emptyLabel);
        }
    }

    private VBox createCrashReportCard(CrashAggregation crash) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #4a2a2a; -fx-background-radius: 5; -fx-border-color: #FF6B6B; -fx-border-width: 2;");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.TOP_LEFT);

        // Utiliser la map pour récupérer le nom du jeu
        String gameName = gameIdToName.getOrDefault(crash.getGameId(), crash.getGameId());
        Label gameLabel = new Label(gameName);
        gameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #FF6B6B;");

        Label dateLabel = new Label(new java.util.Date(crash.getTimestamp()).toString());
        dateLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        headerBox.getChildren().addAll(gameLabel, dateLabel);

        Label errorLabel = new Label("Crashes: " + crash.getCrashCount() + " (window: 5 min)");
        errorLabel.setStyle("-fx-text-fill: #ff9999; -fx-font-size: 12px; -fx-wrap-text: true;");
        errorLabel.setWrapText(true);

        card.getChildren().addAll(headerBox, dateLabel, errorLabel);

        return card;
    }

    private VBox createReviewCard(Review review) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.TOP_LEFT);

        Label gameLabel = new Label(review.getGameName());
        gameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label playerLabel = new Label("Par " + review.getPlayerName());
        playerLabel.setStyle("-fx-text-fill: #aaa;");

        Label ratingLabel = new Label(review.getRatingStars());
        ratingLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 12px;");

        Label timeLabel = new Label(review.getTimestamp().format(formatter));
        timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        headerBox.getChildren().addAll(gameLabel, playerLabel);

        Label commentLabel = new Label(review.getComment());
        commentLabel.setStyle("-fx-text-fill: #ddd; -fx-wrap-text: true;");
        commentLabel.setWrapText(true);

        card.getChildren().addAll(headerBox, ratingLabel, commentLabel, timeLabel);

        return card;
    }

    public void addCrashReport(CrashAggregation crash) {
        System.out.println("[NotificationsTab] Adding crash report for gameId: " + crash.getGameId());
        System.out.println("[NotificationsTab] Game name from map: " + gameIdToName.get(crash.getGameId()));
        System.out.println("[NotificationsTab] Current map contents: " + gameIdToName);
        crashReports.add(0, crash);
        updateView();
    }

    /**
     * Met à jour un crash report existant (même fenêtre) avec un nouveau compte.
     */
    public void updateCrashReport(CrashAggregation updatedCrash) {
        System.out.println("[NotificationsTab] Updating crash report for windowId: " + updatedCrash.getId());
        
        // Trouver et remplacer le crash avec le même ID de fenêtre
        for (int i = 0; i < crashReports.size(); i++) {
            if (crashReports.get(i).getId().equals(updatedCrash.getId())) {
                crashReports.set(i, updatedCrash);
                System.out.println("[NotificationsTab] Crash report updated at index " + i);
                updateView();
                return;
            }
        }
        
        // Si non trouvé, l'ajouter comme nouveau
        System.out.println("[NotificationsTab] Crash report not found, adding as new");
        addCrashReport(updatedCrash);
    }

    /**
     * Charge les crash reports depuis l'API pour les jeux de l'éditeur.
     * Utilisé au démarrage pour récupérer les crashs manqués pendant l'absence.
     */
    public void loadCrashReportsFromApi(String publisherId) {
        if (publisherId == null || publisherId.isEmpty()) {
            System.out.println("[NotificationsTab] No publisher ID to load crashes for");
            return;
        }
        
        new Thread(() -> {
            List<CrashAggregation> allCrashes = new ArrayList<>();
            try {
                // Charger d'abord les jeux pour avoir la correspondance gameId -> gameName
                try {
                    String gamesJson = ApiClient.get("/api/games");
                    List<com.gaming.api.models.GameModel> games = AvroJacksonConfig.avroObjectMapper()
                        .readValue(gamesJson, new TypeReference<List<com.gaming.api.models.GameModel>>() {});
                    for (com.gaming.api.models.GameModel game : games) {
                        gameIdToName.put(game.getGameId(), game.getTitle());
                    }
                } catch (Exception e) {
                    System.err.println("[NotificationsTab] Error loading games: " + e.getMessage());
                }
                
                String json = ApiClient.get("/api/crash-aggregations/publisher/" + publisherId);
                List<CrashAggregation> crashes = AvroJacksonConfig.avroObjectMapper()
                    .readValue(json, new TypeReference<List<CrashAggregation>>() {});
                allCrashes.addAll(crashes);
            } catch (Exception e) {
                System.err.println("[NotificationsTab] Error loading crashes for publisher " + publisherId + ": " + e.getMessage());
            }
            
            // Trier par timestamp décroissant (plus récent en premier)
            allCrashes.sort((c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()));
            
            Platform.runLater(() -> {
                crashReports.clear();
                crashReports.addAll(allCrashes);
                updateView();
                System.out.println("[NotificationsTab] Loaded " + allCrashes.size() + " crash reports from API");
            });
        }).start();
    }

    public List<CrashAggregation> getCrashReports() {
        return crashReports;
    }

    public void addReview(Review review) {
        reviews.add(0, review);
        updateView();
    }
    
    /**
     * Ajoute ou met à jour le nom d'un jeu dans la map
     */
    public void updateGameName(String gameId, String gameName) {
        if (gameId != null && gameName != null) {
            System.out.println("[NotificationsTab] Updating game name: " + gameId + " -> " + gameName);
            gameIdToName.put(gameId, gameName);
        }
    }
}

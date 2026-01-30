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
import java.util.List;
import java.util.stream.Collectors;

public class NotificationsTab extends ScrollPane {

    private VBox notificationsList;
    private List<CrashAggregation> crashReports;
    private List<Review> reviews;
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
        notificationsList.getChildren().clear();

        Label titleLabel = new Label("Notifications");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        notificationsList.getChildren().add(titleLabel);

        // Section Crash Reports
        if (!crashReports.isEmpty()) {
            Label crashTitle = new Label("⚠️ RAPPORTS DE CRASH (" + crashReports.size() + ")");
            crashTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #FF6B6B;");
            notificationsList.getChildren().add(crashTitle);

            for (CrashAggregation crash : crashReports) {
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

        Label gameLabel = new Label(crash.getGame() != null ? crash.getGame().getTitle() : crash.getGameId());
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
        crashReports.add(0, crash);
        updateView();
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
}

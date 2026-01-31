package com.views.components.dialogs;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.model.Game;
import com.model.CrashAggregation;
import com.model.Patch;
import com.model.DLC;
import com.views.components.tabs.NotificationsTab;
import com.util.ApiClient;
import com.util.AvroJacksonConfig;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyGameDialog {

    public static void show(Game game, NotificationsTab notificationsTab) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Détails du jeu - " + game.getTitle());

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");

        // En-tête avec titre du jeu
        Label header = new Label(game.getTitle());
        header.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox headerBox = new HBox(header);
        headerBox.setPadding(new Insets(15));
        headerBox.setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #444; -fx-border-width: 0 0 1 0;");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #2b2b2b;");

        // Onglet 1: Avis
        Tab reviewsTab = new Tab("⭐ Avis", createReviewsTab(game));
        reviewsTab.setClosable(false);

        // Onglet 2: Rapports de crash
        Tab crashesTab = new Tab("⚠️ Crash Reports", createCrashReportsTab(game, notificationsTab));
        crashesTab.setClosable(false);

        // Onglet 3: DLCs & Patches
        Tab contentTab = new Tab("📦 DLCs & Patches", createContentTab(game));
        contentTab.setClosable(false);

        tabPane.getTabs().addAll(reviewsTab, crashesTab, contentTab);

        root.setTop(headerBox);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 800, 650);
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * Onglet Avis - charge les reviews depuis l'API
     */
    private static ScrollPane createReviewsTab(Game game) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: #2b2b2b;");

        Label loadingLabel = new Label("Chargement des avis...");
        loadingLabel.setStyle("-fx-text-fill: #aaa; -fx-font-style: italic;");
        container.getChildren().add(loadingLabel);

        // Charger les reviews en arrière-plan
        new Thread(() -> {
            try {
                String json = ApiClient.get("/api/reviews/game/" + game.getId());
                List<com.gaming.events.GameReviewed> reviews = AvroJacksonConfig.avroObjectMapper()
                    .readValue(json, new TypeReference<List<com.gaming.events.GameReviewed>>() {});

                javafx.application.Platform.runLater(() -> {
                    container.getChildren().clear();

                    if (reviews.isEmpty()) {
                        Label noReviews = new Label("Aucun avis pour ce jeu.");
                        noReviews.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
                        container.getChildren().add(noReviews);
                    } else {
                        // Statistiques
                        double avgRating = reviews.stream().mapToInt(com.gaming.events.GameReviewed::getRating).average().orElse(0);
                        Label stats = new Label(String.format("📊 Note moyenne: %.1f/5 (%d avis)", avgRating, reviews.size()));
                        stats.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16px; -fx-font-weight: bold;");
                        container.getChildren().add(stats);

                        Separator sep = new Separator();
                        sep.setStyle("-fx-background-color: #444;");
                        container.getChildren().add(sep);

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                            .withZone(ZoneId.systemDefault());

                        for (com.gaming.events.GameReviewed review : reviews) {
                            VBox card = createReviewCard(review, formatter);
                            container.getChildren().add(card);
                        }
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    container.getChildren().clear();
                    Label error = new Label("❌ Erreur lors du chargement des avis: " + e.getMessage());
                    error.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14px;");
                    error.setWrapText(true);
                    container.getChildren().add(error);
                });
                System.err.println("[MyGameDialog] Error loading reviews: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");
        return scrollPane;
    }

    private static VBox createReviewCard(com.gaming.events.GameReviewed review, DateTimeFormatter formatter) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-border-color: #555; -fx-border-radius: 5; -fx-border-width: 1;");

        // Ligne 1: Username et étoiles
        HBox header = new HBox(10);
        Label username = new Label(review.getUsername());
        username.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        String stars = getStars(review.getRating());
        Label rating = new Label(stars + " " + review.getRating() + "/5");
        rating.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(username, spacer, rating);

        // Ligne 2: Date
        Label date = new Label(formatter.format(Instant.ofEpochMilli(review.getRegistrationTimestamp())));
        date.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        // Ligne 3: Commentaire
        Label comment = new Label(review.getReviewText());
        comment.setStyle("-fx-text-fill: #ddd; -fx-font-size: 13px;");
        comment.setWrapText(true);

        card.getChildren().addAll(header, date, comment);
        return card;
    }

    /**
     * Onglet Crash Reports - affiche les crashs du jeu
     */
    private static ScrollPane createCrashReportsTab(Game game, NotificationsTab notificationsTab) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: #2b2b2b;");

        if (notificationsTab == null || notificationsTab.getCrashReports() == null) {
            Label noData = new Label("Aucune donnée de crash disponible.");
            noData.setStyle("-fx-text-fill: #999; -fx-font-size: 14px;");
            container.getChildren().add(noData);
        } else {
            List<CrashAggregation> gameCrashes = notificationsTab.getCrashReports().stream()
                .filter(c -> c.getGameId() != null && c.getGameId().equals(game.getId()))
                .sorted((c1, c2) -> Long.compare(c2.getTimestamp(), c1.getTimestamp()))
                .toList();

            if (gameCrashes.isEmpty()) {
                Label noCrashes = new Label("✅ Aucun rapport de crash pour ce jeu.");
                noCrashes.setStyle("-fx-text-fill: #4ade80; -fx-font-size: 14px; -fx-font-weight: bold;");
                container.getChildren().add(noCrashes);
            } else {
                Label header = new Label(String.format("⚠️ %d rapport(s) de crash", gameCrashes.size()));
                header.setStyle("-fx-text-fill: #FF6B6B; -fx-font-size: 16px; -fx-font-weight: bold;");
                container.getChildren().add(header);

                Separator sep = new Separator();
                sep.setStyle("-fx-background-color: #444;");
                container.getChildren().add(sep);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

                for (CrashAggregation crash : gameCrashes) {
                    VBox card = createCrashCard(crash, formatter);
                    container.getChildren().add(card);
                }
            }
        }

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");
        return scrollPane;
    }

    private static VBox createCrashCard(CrashAggregation crash, DateTimeFormatter formatter) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #3c2525; -fx-background-radius: 5; -fx-border-color: #FF6B6B; -fx-border-radius: 5; -fx-border-width: 2;");

        // Nombre de crashs
        Label crashCount = new Label("🔴 " + crash.getCrashCount() + " crash(s) détecté(s)");
        crashCount.setStyle("-fx-text-fill: #FF6B6B; -fx-font-weight: bold; -fx-font-size: 15px;");

        // Fenêtre de temps
        String windowStart = formatter.format(Instant.ofEpochMilli(crash.getWindowStart()));
        String windowEnd = formatter.format(Instant.ofEpochMilli(crash.getWindowEnd()));
        Label window = new Label("⏱️ Fenêtre: " + windowStart + " → " + windowEnd);
        window.setStyle("-fx-text-fill: #ffb3b3; -fx-font-size: 12px;");

        // Timestamp du rapport
        Label timestamp = new Label("📅 Rapport généré: " + formatter.format(Instant.ofEpochMilli(crash.getTimestamp())));
        timestamp.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

        card.getChildren().addAll(crashCount, window, timestamp);
        return card;
    }

    /**
     * Onglet DLCs & Patches
     */
    private static ScrollPane createContentTab(Game game) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: #2b2b2b;");

        // Section DLCs
        Label dlcTitle = new Label("📦 DLCs");
        dlcTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        container.getChildren().add(dlcTitle);

        List<DLC> dlcs = game.getDlcs();
        if (dlcs == null || dlcs.isEmpty()) {
            Label noDlc = new Label("Aucun DLC publié pour ce jeu.");
            noDlc.setStyle("-fx-text-fill: #999; -fx-font-size: 13px;");
            container.getChildren().add(noDlc);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault());

            for (DLC dlc : dlcs) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(10));
                card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-border-color: #555; -fx-border-radius: 5; -fx-border-width: 1;");

                Label name = new Label(dlc.getName());
                name.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

                String dateStr = dlc.getReleaseTimeStamp() != null 
                    ? formatter.format(Instant.ofEpochMilli(dlc.getReleaseTimeStamp())) 
                    : "Date inconnue";
                Label date = new Label("📅 " + dateStr);
                date.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

                Label desc = new Label(dlc.getDescription() != null ? dlc.getDescription() : "Pas de description");
                desc.setStyle("-fx-text-fill: #ddd; -fx-font-size: 12px;");
                desc.setWrapText(true);

                card.getChildren().addAll(name, date, desc);
                container.getChildren().add(card);
            }
        }

        Separator bigSep = new Separator();
        bigSep.setStyle("-fx-background-color: #555; -fx-pref-height: 2;");
        container.getChildren().add(bigSep);

        // Section Patches
        Label patchTitle = new Label("🔧 Patches / Mises à jour");
        patchTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        container.getChildren().add(patchTitle);

        List<Patch> patches = game.getPatches();
        if (patches == null || patches.isEmpty()) {
            Label noPatch = new Label("Aucun patch publié pour ce jeu.");
            noPatch.setStyle("-fx-text-fill: #999; -fx-font-size: 13px;");
            container.getChildren().add(noPatch);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault());

            for (Patch patch : patches) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(10));
                card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-border-color: #555; -fx-border-radius: 5; -fx-border-width: 1;");

                Label version = new Label("v" + patch.getVersion());
                version.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-font-size: 14px;");

                String dateStr = patch.getReleaseTimeStamp() != null 
                    ? formatter.format(Instant.ofEpochMilli(patch.getReleaseTimeStamp())) 
                    : "Date inconnue";
                Label date = new Label("📅 " + dateStr);
                date.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

                Label desc = new Label(patch.getDescription() != null ? patch.getDescription() : "Pas de notes de version");
                desc.setStyle("-fx-text-fill: #ddd; -fx-font-size: 12px;");
                desc.setWrapText(true);

                card.getChildren().addAll(version, date, desc);
                container.getChildren().add(card);
            }
        }

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");
        return scrollPane;
    }

    private static String getStars(int rating) {
        return "★".repeat(Math.max(0, rating)) + "☆".repeat(Math.max(0, 5 - rating));
    }
}
package com.views.components.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.model.Game;
import com.model.Patch;
import com.model.DLC;
import com.views.components.dialogs.*;
import com.gaming.api.models.PublisherModel;
import com.gaming.api.requests.GameReleased;
import com.gaming.api.models.GameModel;
import com.util.ApiClient;
import com.util.AvroJacksonConfig;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.List;

public class MyGamesTab extends ScrollPane {

    private FlowPane gameGrid;
    private List<Game> publishedGames;
    private Runnable onUpdate;
    private NotificationsTab notificationsTab;
    private PublisherModel currentPublisher;

    public MyGamesTab(PublisherModel currentPublisher, NotificationsTab notificationsTab, Runnable onUpdate) {
        this.currentPublisher = currentPublisher;
        this.publishedGames = new ArrayList<>();
        this.onUpdate = onUpdate;
        this.notificationsTab = notificationsTab;

        gameGrid = new FlowPane();
        gameGrid.setHgap(15);
        gameGrid.setVgap(15);
        gameGrid.setPadding(new Insets(20));
        gameGrid.setStyle("-fx-background-color: #2b2b2b;");

        // Charger les jeux depuis l'API
        loadGamesFromApi();
        
        updateView();

        this.setContent(gameGrid);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: #2b2b2b;");
    }

    private void updateView() {
        // Vider toutes les cartes de jeux (garder seulement le bouton de publication à l'index 0)
        gameGrid.getChildren().clear();
        
        // Rajouter le bouton de publication
        Button publishGameButton = new Button("Publier un jeu");
        publishGameButton.setPrefWidth(350);
        publishGameButton.setPrefHeight(180);
        publishGameButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        publishGameButton.setOnAction(e -> {
            PublishGameDialog.PublishedGameData data = PublishGameDialog.show();
            if (data != null) {
                publishGameToApi(data);
            }
        });
        gameGrid.getChildren().add(publishGameButton);
        
        // Ajouter toutes les cartes de jeux
        for (Game game : publishedGames) {
            VBox gameCard = createGameCard(game);
            gameGrid.getChildren().add(gameCard);
        }
    }

    private VBox createGameCard(Game game) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-cursor: hand;");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(350);

        Label nameLabel = new Label(game.getTitle());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label platformLabel = new Label("Support: " + game.getPlatform());
        platformLabel.setStyle("-fx-text-fill: #aaa;");

        Label genreLabel = new Label("Genres: " + game.getGenre());
        genreLabel.setStyle("-fx-text-fill: #aaa;");

        // Statistiques: note moyenne (chargée depuis l'API)
        Label statsLabel = new Label("Chargement de la note...");
        statsLabel.setStyle("-fx-text-fill: #ddd; -fx-font-size: 12px;");
        
        // Charger la note moyenne depuis l'API en arrière-plan
        loadGameRating(game.getId(), statsLabel);

        // Boutons d'action
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPadding(new Insets(10, 0, 0, 0));

        Button patchButton = new Button("Patch");
        patchButton.setPrefWidth(100);
        patchButton.setStyle("-fx-font-size: 11px;");
        patchButton.setOnAction(e -> {
            PublishPatchDialog.PatchData patchData = PublishPatchDialog.show(game.getId());
            if (patchData != null) {
                Patch patch = new Patch();
                patch.setGame(game);
                patch.setVersion(patchData.version);
                patch.setDescription(patchData.comment);
                patch.setReleaseTimeStamp(System.currentTimeMillis());
                if (game.getPatches() == null) game.setPatches(new java.util.ArrayList<>());
                game.getPatches().add(patch);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Patch v" + patchData.version + " publié avec succès!");
                alert.showAndWait();
            }
        });

        Button dlcButton = new Button("DLC");
        dlcButton.setPrefWidth(100);
        dlcButton.setStyle("-fx-font-size: 11px;");
        dlcButton.setOnAction(e -> {
            PublishDLCDialog.DLCData dlcData = PublishDLCDialog.show(game.getId());
            if (dlcData != null) {
                DLC dlc = new DLC();
                dlc.setGame(game);
                dlc.setName(dlcData.name);
                dlc.setDescription(dlcData.description);
                dlc.setReleaseTimeStamp(System.currentTimeMillis());
                if (game.getDlcs() == null) game.setDlcs(new java.util.ArrayList<>());
                game.getDlcs().add(dlc);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("DLC '" + dlcData.name + "' publié avec succès!");
                alert.showAndWait();
            }
        });

        actionsBox.getChildren().addAll(patchButton, dlcButton);

        card.getChildren().addAll(nameLabel, platformLabel, genreLabel, statsLabel, actionsBox);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 5; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-cursor: hand;"));

        // Ouvrir la fenêtre détaillée du jeu
        card.setOnMouseClicked(e -> {
            com.views.components.dialogs.MyGameDialog.show(game, notificationsTab);
        });

        return card;
    }

    public void addPublishedGame(Game game) {
        if (!publishedGames.contains(game)) {
            publishedGames.add(game);
        }
        updateView();
    }

    public List<Game> getPublishedGames() {
        return publishedGames;
    }

    private void loadGamesFromApi() {
        try {
            String responseJson = ApiClient.get("/api/games/publisher/"+currentPublisher.getId());
            List<GameModel> gameModels = AvroJacksonConfig.avroObjectMapper()
                .readValue(responseJson, new TypeReference<List<GameModel>>() {});
            
            publishedGames.clear();
            for (GameModel gameModel : gameModels) {
                Game game = new Game();
                game.setId(gameModel.getGameId());
                game.setTitle(gameModel.getTitle());
                game.setPlatform(gameModel.getPlatform());
                game.setGenre(gameModel.getGenre());
                game.setReleaseTimeStamp(gameModel.getReleaseTimeStamp());
                game.setPrice(gameModel.getPrice());
                game.setVersion(gameModel.getVersion());
                // initialize relational lists
                game.setCrashes(new ArrayList<>());
                game.setPatches(new ArrayList<>());
                game.setDlcs(new ArrayList<>());
                publishedGames.add(game);
            }
            updateView();
        } catch (Exception ex) {
            System.out.println("[LOAD GAMES] Error loading games from API:");
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors du chargement des jeux: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    private void publishGameToApi(PublishGameDialog.PublishedGameData data) {
        try {
            GameReleased gameModel = GameReleased.newBuilder()
                .setTitle(data.name)
                .setPublisherName(currentPublisher.getName())
                .setPublisherId(currentPublisher.getId())
                .setPlatform(data.platform)
                .setGenre(String.join(", ", data.genres))
                .setReleaseTimeStamp(System.currentTimeMillis())
                .setPrice(data.price)
                .setVersion(data.version)
                .setDescription(data.description)
                .build();
            
            String json = AvroJacksonConfig.avroObjectMapper().writeValueAsString(gameModel);
            String responseJson = ApiClient.postJson("/api/games/publish", json);
            
            // Recharger tous les jeux après publication
            loadGamesFromApi();
            
            if (onUpdate != null) onUpdate.run();
            
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setContentText("Jeu publié avec succès!");
            success.showAndWait();
        } catch (Exception ex) {
            System.out.println("[PUBLISH GAME] Error publishing game:");
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la publication du jeu: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Charge la note moyenne d'un jeu depuis l'API
     */
    private void loadGameRating(String gameId, Label statsLabel) {
        new Thread(() -> {
            try {
                System.out.println("[MyGamesTab] Loading rating for game: " + gameId);
                
                // Charger les reviews pour la note moyenne
                String reviewsJson = ApiClient.get("/api/reviews/game/" + gameId);
                System.out.println("[MyGamesTab] Reviews JSON response: " + reviewsJson);
                
                List<com.gaming.events.GameReviewed> reviews = AvroJacksonConfig.avroObjectMapper()
                    .readValue(reviewsJson, new TypeReference<List<com.gaming.events.GameReviewed>>() {});
                
                System.out.println("[MyGamesTab] Parsed " + reviews.size() + " reviews");
                
                // Calculer la note moyenne
                if (reviews.isEmpty()) {
                    javafx.application.Platform.runLater(() -> {
                        statsLabel.setText("⭐ Note: N/A (aucun avis)");
                    });
                    return;
                }
                
                double avgRating = 0.0;
                for (com.gaming.events.GameReviewed review : reviews) {
                    avgRating += review.getRating();
                }
                avgRating = avgRating / reviews.size();
                
                System.out.println("[MyGamesTab] Average rating: " + avgRating);
                
                String stars = getStars(avgRating);
                String finalText = "⭐ Note: " + stars + " " + String.format("%.1f", avgRating) + "/5 (" + reviews.size() + " avis)";
                
                javafx.application.Platform.runLater(() -> {
                    statsLabel.setText(finalText);
                });
            } catch (Exception e) {
                System.err.println("[MyGamesTab] Error loading rating for game " + gameId + ": " + e.getMessage());
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    statsLabel.setText("⭐ Note: N/A (erreur de chargement)");
                });
            }
        }).start();
    }
    
    /**
     * Convertit une note en étoiles visuelles
     */
    private String getStars(double rating) {
        int fullStars = (int) rating;
        boolean hasHalfStar = (rating - fullStars) >= 0.5;
        int emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
        
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        if (hasHalfStar) {
            stars.append("☆");
        }
        for (int i = 0; i < emptyStars; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }
}

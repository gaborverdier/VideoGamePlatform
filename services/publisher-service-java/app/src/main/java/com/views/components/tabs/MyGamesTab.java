package com.views.components.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.model.Game;
import com.model.Patch;
import com.model.DLC;
import com.views.components.dialogs.*;

import java.util.ArrayList;
import java.util.List;

public class MyGamesTab extends ScrollPane {

    private FlowPane gameGrid;
    private List<Game> publishedGames;
    private Runnable onUpdate;
    private NotificationsTab notificationsTab;

    public MyGamesTab(NotificationsTab notificationsTab, Runnable onUpdate) {
        this.publishedGames = new ArrayList<>();
        this.onUpdate = onUpdate;
        this.notificationsTab = notificationsTab;

        gameGrid = new FlowPane();
        gameGrid.setHgap(15);
        gameGrid.setVgap(15);
        gameGrid.setPadding(new Insets(20));
        gameGrid.setStyle("-fx-background-color: #2b2b2b;");

        // Bouton pour publier un jeu
        VBox publishContainer = new VBox();
        publishContainer.setPadding(new Insets(20));
        
        Button publishGameButton = new Button("Publier un jeu");
        publishGameButton.setPrefWidth(350);
        publishGameButton.setPrefHeight(180);
        publishGameButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        publishGameButton.setOnAction(e -> {
            PublishGameDialog.PublishedGameData data = PublishGameDialog.show();
            if (data != null) {
                Game game = new Game();
                game.setTitle(data.name);
                game.setPlatform(data.platform);
                game.setGenre(String.join(", ", data.genres));
                // initialize relational lists so UI can append to them
                game.setCrashes(new java.util.ArrayList<>());
                game.setPatches(new java.util.ArrayList<>());
                game.setDlcs(new java.util.ArrayList<>());
                publishedGames.add(game);
                updateView();
                if (onUpdate != null) onUpdate.run();
            }
        });
        
        gameGrid.getChildren().add(publishGameButton);
        
        updateView();

        this.setContent(gameGrid);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: #2b2b2b;");
    }

    private void updateView() {
        if (publishedGames.isEmpty() && gameGrid.getChildren().size() <= 1) {
            Label emptyLabel = new Label("Aucun jeu publié pour le moment.\nCliquez sur 'Publier un jeu' pour commencer!");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaa;");
            emptyLabel.setAlignment(Pos.CENTER);
        } else {
            // Garder le bouton de publication
            while (gameGrid.getChildren().size() > publishedGames.size() + 1) {
                gameGrid.getChildren().remove(1);
            }
            
            for (int i = gameGrid.getChildren().size() - 1; i < publishedGames.size(); i++) {
                Game game = publishedGames.get(i);
                VBox gameCard = createGameCard(game);
                gameGrid.getChildren().add(gameCard);
            }
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

        // Statistiques: note moyenne et temps de jeu moyen
        double avgRating = 0.0;
        double avgPlaytime = 0.0;
        int ratingCount = 0;
        int playtimeCount = 0;
        if (notificationsTab != null && notificationsTab.getReviews() != null) {
            for (com.model.Review r : notificationsTab.getReviews()) {
                if (r.getGameName() != null && r.getGameName().equals(game.getTitle())) {
                    ratingCount++;
                    avgRating += r.getRating();
                    if (r.getPlaytimeMinutes() != null) {
                        playtimeCount++;
                        avgPlaytime += r.getPlaytimeMinutes();
                    }
                }
            }
        }
        String avgRatingText = ratingCount > 0 ? String.format("%.2f", avgRating / ratingCount) : "N/A";
        String avgPlaytimeText = playtimeCount > 0 ? String.format("%d min", (int)Math.round(avgPlaytime / playtimeCount)) : "N/A";

        Label statsLabel = new Label("Note moyenne: " + avgRatingText + "   |   Temps de jeu moyen: " + avgPlaytimeText);
        statsLabel.setStyle("-fx-text-fill: #ddd; -fx-font-size: 12px;");

        // Boutons d'action
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER);
        actionsBox.setPadding(new Insets(10, 0, 0, 0));

        Button patchButton = new Button("Patch");
        patchButton.setPrefWidth(100);
        patchButton.setStyle("-fx-font-size: 11px;");
        patchButton.setOnAction(e -> {
            PublishPatchDialog.PatchData patchData = PublishPatchDialog.show();
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
            PublishDLCDialog.DLCData dlcData = PublishDLCDialog.show();
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
}

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

    public MyGamesTab(Runnable onUpdate) {
        this.publishedGames = new ArrayList<>();
        this.onUpdate = onUpdate;

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
                patch.setReleaseDate(java.time.LocalDate.now().toString());
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
                dlc.setReleaseDate(java.time.LocalDate.now().toString());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("DLC '" + dlcData.name + "' publié avec succès!");
                alert.showAndWait();
            }
        });

        actionsBox.getChildren().addAll(patchButton, dlcButton);

        card.getChildren().addAll(nameLabel, platformLabel, genreLabel, actionsBox);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 5; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-cursor: hand;"));

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

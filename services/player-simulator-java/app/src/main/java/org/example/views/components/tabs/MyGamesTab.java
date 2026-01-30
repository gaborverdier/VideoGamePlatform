package org.example.views.components.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.models.Game;
import org.example.views.components.dialogs.OwnedGameDetailsDialog;

import java.util.ArrayList;
import java.util.List;
import org.example.services.GameDataService;
import org.example.services.SessionManager;

public class MyGamesTab extends ScrollPane {

    private FlowPane gameGrid;
    private List<Game> ownedGames;
    private Runnable onUpdate;

    public MyGamesTab(Runnable onUpdate) {
        this.ownedGames = new ArrayList<>();
        this.onUpdate = onUpdate;

        gameGrid = new FlowPane();
        gameGrid.setHgap(15);
        gameGrid.setVgap(15);
        gameGrid.setPadding(new Insets(20));
        gameGrid.setStyle("-fx-background-color: #2b2b2b;");

        // load owned games for current user
        String userId = null;
        if (SessionManager.getInstance().getCurrentPlayer() != null) {
            userId = SessionManager.getInstance().getCurrentPlayer().getId();
        }
        if (userId != null) {
            this.ownedGames = GameDataService.getInstance().getUserLibrary(userId);
        }
        updateView();

        this.setContent(gameGrid);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: #2b2b2b;");
    }

    private void updateView() {
        gameGrid.getChildren().clear();

        if (ownedGames.isEmpty()) {
            Label emptyLabel = new Label("Aucun jeu acheté pour le moment.\nAllez dans la Bibliothèque pour acheter des jeux !");
            emptyLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #aaa; -fx-padding: 50;");
            emptyLabel.setAlignment(Pos.CENTER);
            gameGrid.getChildren().add(emptyLabel);
        } else {
            for (Game game : ownedGames) {
                VBox gameCard = createOwnedGameCard(game);
                gameGrid.getChildren().add(gameCard);
            }
        }
    }

    private VBox createOwnedGameCard(Game game) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-cursor: hand;");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(350);

        ImageView imageView = new ImageView(game.getCoverImage());
        imageView.setFitWidth(330);
        imageView.setFitHeight(155);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(game.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label genreLabel = new Label(game.getGenre());
        genreLabel.setStyle("-fx-text-fill: #aaa;");

        Label platformLabel = new Label("Support: " + game.getPlatform());
        platformLabel.setStyle("-fx-text-fill: #aaa;");

        // Status installé ou non
        Label statusLabel = new Label(game.isInstalled() ? "✅ Installé" : "⬇ Pas installé");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + (game.isInstalled() ? "#4CAF50" : "#FF9800") + ";");

        card.getChildren().addAll(imageView, nameLabel, genreLabel, platformLabel, statusLabel);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 5; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-cursor: hand;"));

        card.setOnMouseClicked(e -> {
            OwnedGameDetailsDialog.show(game, () -> {
                updateView();
                this.refresh();
                if (onUpdate != null) onUpdate.run();
            });
        });

        return card;
    }

    public void addOwnedGame(Game game) {
        if (!ownedGames.contains(game)) {
            ownedGames.add(game);
        }
        updateView();
    }

    public List<Game> getOwnedGames() {
        return new ArrayList<>(ownedGames);
    }
    
    public void refresh() {
        String userId = null;
        if (SessionManager.getInstance().getCurrentPlayer() != null) {
            userId = SessionManager.getInstance().getCurrentPlayer().getId();
        }
        if (userId != null) {
            this.ownedGames = GameDataService.getInstance().getUserLibrary(userId);
        }
        updateView();
    }
}

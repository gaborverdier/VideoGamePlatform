package org.example. views.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene. control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.models.Game;

import java.util.ArrayList;
import java.util.List;

public class MyGamesTab extends ScrollPane {

    private FlowPane gameGrid;
    private List<Game> ownedGames;

    public MyGamesTab() {
        this. ownedGames = new ArrayList<>();

        // FlowPane pour afficher les jeux possédés
        gameGrid = new FlowPane();
        gameGrid.setHgap(15);
        gameGrid.setVgap(15);
        gameGrid.setPadding(new Insets(20));
        gameGrid.setStyle("-fx-background-color: #2b2b2b;");

        // Message par défaut si aucun jeu
        updateView();

        // Configuration du ScrollPane
        this.setContent(gameGrid);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: #2b2b2b;");
    }

    // Mettre à jour la vue
    private void updateView() {
        gameGrid.getChildren().clear();

        if (ownedGames.isEmpty()) {
            Label emptyLabel = new Label("Aucun jeu acheté pour le moment.\nAllez dans la Bibliothèque pour acheter des jeux !");
            emptyLabel.setStyle("-fx-font-size:  18px; -fx-text-fill: #aaa; -fx-padding: 50;");
            emptyLabel.setAlignment(Pos.CENTER);
            gameGrid.getChildren().add(emptyLabel);
        } else {
            for (Game game : ownedGames) {
                VBox gameCard = createOwnedGameCard(game);
                gameGrid.getChildren().add(gameCard);
            }
        }
    }

    // Créer une carte pour un jeu possédé
    private VBox createOwnedGameCard(Game game) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-cursor: hand;");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(350);

        // Image du jeu
        ImageView imageView = new ImageView(game.getCoverImage());
        imageView.setFitWidth(330);
        imageView.setFitHeight(155);
        imageView.setPreserveRatio(true);

        // Nom du jeu
        Label nameLabel = new Label(game.getName());
        nameLabel. setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Genre
        Label genreLabel = new Label(game.getGenre());
        genreLabel.setStyle("-fx-text-fill: #aaa;");

        // Badge "Possédé"
        Label ownedBadge = new Label("✓ POSSÉDÉ");
        ownedBadge.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        card.getChildren().addAll(imageView, nameLabel, genreLabel, ownedBadge);

        // Effet hover
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 5; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-cursor: hand;"));

        // Click pour lancer le jeu (simulé)
        card.setOnMouseClicked(e -> {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Lancer le jeu");
            info.setHeaderText(game.getName());
            info.setContentText("Lancement du jeu en cours...");
            info.showAndWait();
        });

        return card;
    }

    // Ajouter un jeu acheté (appelé depuis le contrôleur)
    public void addOwnedGame(Game game) {
        if (! ownedGames.contains(game)) {
            ownedGames.add(game);
            updateView();
        }
    }

    // Récupérer les jeux possédés
    public List<Game> getOwnedGames() {
        return new ArrayList<>(ownedGames);
    }
}
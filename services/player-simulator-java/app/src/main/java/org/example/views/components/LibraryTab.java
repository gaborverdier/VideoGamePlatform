package org.example.views.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.models.Game;

import java.util.List;
import java.util.function.Consumer;

public class LibraryTab extends ScrollPane {

    private FlowPane gameGrid;
    private Consumer<Game> onGamePurchased;

    public LibraryTab(Consumer<Game> onGamePurchased) {
        this.onGamePurchased = onGamePurchased;
        
        gameGrid = new FlowPane();
        gameGrid.setHgap(15);
        gameGrid.setVgap(15);
        gameGrid.setPadding(new Insets(20));
        gameGrid.setStyle("-fx-background-color: #2b2b2b;");

        loadGames();

        this.setContent(gameGrid);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: #2b2b2b;");
    }

    private void loadGames() {
        List<Game> games = List.of(
            new Game("Elden Ring", 59.99, "RPG", 
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1245620/header.jpg",
                "Un RPG d'action en monde ouvert dans les Terres Intermédiaires.  Explorez un vaste monde et affrontez des boss épiques.",
                4.8, 80),
            new Game("Minecraft", 29.99, "Sandbox", 
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1276930/header.jpg",
                "Jeu de construction et d'aventure en monde ouvert. Créez, explorez et survivez dans un univers de blocs.",
                4.5, 200),
            new Game("FIFA 24", 69.99, "Sport", 
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/2195250/header.jpg",
                "Le jeu de football le plus réaliste.  Jouez avec vos équipes favorites et participez à des tournois.",
                4.0, 50),
            new Game("The Witcher 3", 39.99, "RPG", 
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/292030/header.jpg",
                "Incarnez Geralt de Riv et partez dans une quête épique à travers un monde fantastique immense.",
                4.9, 120),
            new Game("GTA V", 29.99, "Action", 
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/271590/header.jpg",
                "Explorez Los Santos dans ce jeu d'action en monde ouvert. Missions, courses et chaos urbain vous attendent.",
                4.7, 100),
            new Game("Cyberpunk 2077", 49.99, "RPG", 
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/1091500/header.jpg",
                "RPG futuriste dans la mégalopole de Night City. Personnalisez votre personnage et tracez votre chemin.",
                4.2, 60),
            new Game("CS:GO", 0.00, "FPS", 
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/header.jpg",
                "Jeu de tir tactique en équipe. Affrontez des adversaires dans des modes compétitifs.",
                4.6, 300),
            new Game("Valorant", 0.00, "FPS", 
                "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/730/header.jpg",
                "FPS tactique 5v5 avec des agents aux capacités uniques. Stratégie et précision sont la clé.",
                4.4, 150)
        );

        for (Game game : games) {
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

        ImageView imageView = new ImageView(game.getCoverImage());
        imageView.setFitWidth(330);
        imageView.setFitHeight(155);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(game.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label genreLabel = new Label(game.getGenre());
        genreLabel.setStyle("-fx-text-fill: #aaa;");

        Label priceLabel = new Label(game. getFormattedPrice());
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        card.getChildren().addAll(imageView, nameLabel, genreLabel, priceLabel);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 5; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-cursor: hand;"));

        // Ouvrir les détails au clic
        card.setOnMouseClicked(e -> {
            GameDetailsDialog dialog = new GameDetailsDialog(game);
            boolean purchased = dialog.show();
            
            if (purchased) {
                onGamePurchased.accept(game);
            }
        });

        return card;
    }
}
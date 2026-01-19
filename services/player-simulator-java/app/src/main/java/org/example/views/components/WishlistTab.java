package org. example.views.components;

import javafx.geometry.Insets;
import javafx.geometry. Pos;
import javafx. scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.models.Game;
import org.example.services.GameDataService;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream. Collectors;

public class WishlistTab extends ScrollPane {

    private FlowPane gameGrid;
    private Consumer<Game> onGamePurchased;

    public WishlistTab(Consumer<Game> onGamePurchased) {
        this.onGamePurchased = onGamePurchased;
        
        gameGrid = new FlowPane();
        gameGrid.setHgap(15);
        gameGrid.setVgap(15);
        gameGrid.setPadding(new Insets(20));
        gameGrid.setStyle("-fx-background-color: #2b2b2b;");

        updateView();

        this.setContent(gameGrid);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: #2b2b2b;");
    }

    public void updateView() {
        gameGrid. getChildren().clear();

        List<Game> wishlistedGames = GameDataService. getInstance().getAllGames().stream()
            .filter(Game::isWishlisted)
            .collect(Collectors.toList());

        if (wishlistedGames. isEmpty()) {
            Label emptyLabel = new Label("Aucun jeu dans votre liste de souhaits.\nAjoutez des jeux depuis la Bibliothèque !");
            emptyLabel.setStyle("-fx-font-size:  18px; -fx-text-fill: #aaa; -fx-padding: 50;");
            emptyLabel.setAlignment(Pos.CENTER);
            gameGrid.getChildren().add(emptyLabel);
        } else {
            for (Game game :  wishlistedGames) {
                VBox gameCard = createWishlistCard(game);
                gameGrid.getChildren().add(gameCard);
            }
        }
    }

    private VBox createWishlistCard(Game game) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius:  5; -fx-cursor:  hand;");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(350);

        ImageView imageView = new ImageView(game. getCoverImage());
        imageView.setFitWidth(330);
        imageView.setFitHeight(155);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(game.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight:  bold; -fx-text-fill: white;");

        Label genreLabel = new Label(game. getGenre());
        genreLabel.setStyle("-fx-text-fill: #aaa;");

        Label priceLabel = new Label(game.getFormattedPrice());
        priceLabel. setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        Label wishlistBadge = new Label("❤ Liste de souhaits");
        wishlistBadge.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");

        card.getChildren().addAll(imageView, nameLabel, genreLabel, priceLabel, wishlistBadge);

        card.setOnMouseEntered(e -> card. setStyle("-fx-background-color: #4a4a4a; -fx-background-radius:  5; -fx-cursor:  hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-cursor: hand;"));

        card.setOnMouseClicked(e -> {
            GameDetailsDialog dialog = new GameDetailsDialog(game);
            boolean purchased = dialog.show();
            
            if (purchased && onGamePurchased != null) {
                onGamePurchased.accept(game);
            }
            
            updateView();
        });

        return card;
    }
    
    public void refresh() {
        updateView();
    }
}
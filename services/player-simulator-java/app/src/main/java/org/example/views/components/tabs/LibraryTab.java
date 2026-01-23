package org.example.views.components.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.models.Game;
import org.example.services.GameDataService;
import org.example.views.components.dialogs.GameDetailsDialog;

import java.util.List;
import java.util.function.Consumer;

public class LibraryTab extends ScrollPane {

    private FlowPane gameGrid;
    private Consumer<Game> onGamePurchased;
    private List<Game> games;
    private Runnable onRefreshAll;

    public LibraryTab(Consumer<Game> onGamePurchased) {
        this.onGamePurchased = onGamePurchased;
        this.games = org.example.services.GameDataService.getInstance().getAllGames();
        
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

    public void setOnRefreshAll(Runnable onRefreshAll) {
        this.onRefreshAll = onRefreshAll;
    }

    private void loadGames() {
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

        Label priceLabel = new Label(game.getFormattedPrice());
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

        card.getChildren().addAll(imageView, nameLabel, genreLabel, priceLabel);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 5; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-cursor: hand;"));

        // Ouvrir les détails au clic
        card.setOnMouseClicked(e -> {
            Runnable wishlistCallback = onRefreshAll != null ? onRefreshAll : this::refresh;
            GameDetailsDialog dialog = new GameDetailsDialog(game, wishlistCallback);
            boolean purchased = dialog.show();
            
            if (purchased && onGamePurchased != null) {
                onGamePurchased.accept(game);
            }
        });

        return card;
    }
    
    public void refresh() {
        gameGrid.getChildren().clear();
        this.games = org.example.services.GameDataService.getInstance().getAllGames();
        loadGames();
    }
    
    public List<Game> getGames() {
        return games;
    }
}

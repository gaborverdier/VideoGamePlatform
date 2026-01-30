package org.example.views.components.tabs;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.example.models.Game;
import org.example.views.components.dialogs.GameDetailsDialog;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class LibraryTab extends VBox {

    private FlowPane gameGrid;
    private Consumer<Game> onGamePurchased;
    private List<Game> games;
    private List<Game> filteredGames;
    private Runnable onRefreshAll;
    private TextField searchField;
    private Label resultsLabel;

    public LibraryTab(Consumer<Game> onGamePurchased) {
        this.onGamePurchased = onGamePurchased;
        this.games = org.example.services.GameDataService.getInstance().getAllGames();
        this.filteredGames = this.games;
        
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #2b2b2b;");
        
        // Titre et barre de recherche
        Label titleLabel = new Label("🛒 Magasin");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(10, 0, 10, 0));
        
        searchField = new TextField();
        searchField.setPromptText("Rechercher un jeu...");
        searchField.setPrefWidth(300);
        searchField.setOnKeyReleased(e -> filterGames());
        
        Button clearBtn = new Button("✖ Effacer");
        clearBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            filterGames();
        });
        
        searchBox.getChildren().addAll(searchField, clearBtn);
        
        resultsLabel = new Label(filteredGames.size() + " jeu(x) disponible(s)");
        resultsLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14px;");
        
        gameGrid = new FlowPane();
        gameGrid.setHgap(15);
        gameGrid.setVgap(15);
        gameGrid.setPadding(new Insets(10));
        gameGrid.setStyle("-fx-background-color: #2b2b2b;");

        ScrollPane scrollPane = new ScrollPane(gameGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        loadGames();

        getChildren().addAll(titleLabel, searchBox, resultsLabel, scrollPane);
    }

    public void setOnRefreshAll(Runnable onRefreshAll) {
        this.onRefreshAll = onRefreshAll;
    }

    private void filterGames() {
        String searchText = searchField.getText().toLowerCase().trim();
        
        if (searchText.isEmpty()) {
            filteredGames = games;
        } else {
            filteredGames = games.stream()
                .filter(game -> game.getName().toLowerCase().contains(searchText) ||
                               game.getGenre().toLowerCase().contains(searchText) ||
                               game.getPublisherName().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
        }
        
        resultsLabel.setText(filteredGames.size() + " jeu(x) trouvé(s)");
        loadGames();
    }

    private void loadGames() {
        gameGrid.getChildren().clear();
        for (Game game : filteredGames) {
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
        this.games = org.example.services.GameDataService.getInstance().getAllGames();
        searchField.clear();
        filterGames();
    }
    
    public List<Game> getGames() {
        return games;
    }
}

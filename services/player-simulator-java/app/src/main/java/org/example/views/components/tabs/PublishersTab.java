package org.example.views.components.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.models.Game;
import org.example.services.GameDataService;
import org.example.views.components.dialogs.GameDetailsDialog;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PublishersTab extends ScrollPane {

    private VBox container;
    private Consumer<Game> onGamePurchased;

    public PublishersTab(Consumer<Game> onGamePurchased) {
        this.onGamePurchased = onGamePurchased;
        
        container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #2b2b2b;");

        this.setContent(container);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: #2b2b2b;");

        refresh();
    }

    public void refresh() {
        container.getChildren().clear();
        
        List<Game> allGames = GameDataService.getInstance().getAllGames();
        Map<String, List<Game>> byPublisher = allGames.stream()
                .collect(Collectors.groupingBy(
                    Game::getPublisherName, 
                    LinkedHashMap::new, 
                    Collectors.toList()
                ));

        for (Map.Entry<String, List<Game>> entry : byPublisher.entrySet()) {
            String publisherName = entry.getKey();
            List<Game> games = entry.getValue().stream()
                    .sorted(Comparator.comparing(Game::getName))
                    .collect(Collectors.toList());
            
            VBox publisherCard = createPublisherCard(publisherName, games);
            container.getChildren().add(publisherCard);
        }
    }

    private VBox createPublisherCard(String publisherName, List<Game> games) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");

        // En-tête éditeur
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label(publisherName);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label countLabel = new Label(games.size() + " jeu(x)");
        countLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14px;");
        
        header.getChildren().addAll(nameLabel, spacer, countLabel);

        // Liste des jeux
        VBox gamesList = new VBox(8);
        for (Game game : games) {
            HBox gameRow = createGameRow(game);
            gamesList.getChildren().add(gameRow);
        }

        card.getChildren().addAll(header, new Separator(), gamesList);
        return card;
    }

    private HBox createGameRow(Game game) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5));
        row.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 3;");

        Label nameLabel = new Label(game.getName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        nameLabel.setMinWidth(200);

        Label genreLabel = new Label(game.getGenre());
        genreLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13px;");
        genreLabel.setMinWidth(100);

        Label platformsLabel = new Label(game.getPlatform());
        platformsLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label priceLabel = new Label(game.getFormattedPrice());
        priceLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14px;");

        Button detailsBtn = new Button("Détails");
        detailsBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
        detailsBtn.setOnAction(e -> {
            GameDetailsDialog dialog = new GameDetailsDialog(game, this::refresh);
            boolean purchased = dialog.show();
            if (purchased && onGamePurchased != null) {
                onGamePurchased.accept(game);
            }
        });

        row.getChildren().addAll(nameLabel, genreLabel, platformsLabel, spacer, priceLabel, detailsBtn);

        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 3;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 3;"));

        return row;
    }
}

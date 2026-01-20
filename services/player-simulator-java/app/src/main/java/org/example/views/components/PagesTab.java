package org.example.views.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.models.Game;
import org.example.models.Player;
import org.example.services.GameDataService;
import org.example.services.SessionManager;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PagesTab extends ScrollPane {

    private VBox container;

    public PagesTab() {
        container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #2b2b2b;");

        this.setContent(container);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: #2b2b2b;");

        refresh();
    }

    public void refresh() {
        container.getChildren().clear();
        container.getChildren().addAll(buildUserSection(), new Separator(), buildGamesSection(), new Separator(), buildPublishersSection());
    }

    private TitledPane buildUserSection() {
        Player player = SessionManager.getInstance().getCurrentPlayer();
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        Label name = styledLabel("Utilisateur : " + (player != null ? player.getUsername() : "-"), 16, true);
        Label wallet = styledLabel("Solde : " + formatEuro(player != null ? player.getWallet() : 0), 14, false);
        int totalTime = player != null ? player.getOwnedGames().stream().mapToInt(Game::getPlayedTime).sum() : 0;
        Label time = styledLabel("Temps de jeu total : " + totalTime + " min", 14, false);

        VBox library = new VBox(6);
        if (player != null && !player.getOwnedGames().isEmpty()) {
            for (Game g : player.getOwnedGames()) {
                Label line = styledLabel("• " + g.getName() + " — " + g.getPlayedTime() + " min — " + g.getOwnedPlatformsLabel(), 13, false);
                library.getChildren().add(line);
            }
        } else {
            library.getChildren().add(styledLabel("Aucun jeu possédé.", 13, false));
        }

        box.getChildren().addAll(name, wallet, time, new Separator(), styledLabel("Bibliothèque personnelle", 14, true), library);
        TitledPane pane = new TitledPane("Page utilisateur", box);
        pane.setExpanded(true);
        return pane;
    }

    private TitledPane buildGamesSection() {
        List<Game> games = GameDataService.getInstance().getAllGames()
                .stream()
                .sorted(Comparator.comparing(Game::getName))
                .collect(Collectors.toList());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        int row = 0;
        for (Game g : games) {
            Label name = styledLabel(g.getName(), 14, true);
            Label meta = styledLabel(g.getGenre() + " • " + g.getPublisherName() + " • " + g.getSupportedPlatformsLabel(), 12, false);
            Label price = styledLabel(g.getFormattedPrice(), 13, true);
            price.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");

            grid.add(name, 0, row);
            grid.add(meta, 1, row);
            grid.add(price, 2, row);
            row++;
        }

        TitledPane pane = new TitledPane("Pages jeux", grid);
        pane.setExpanded(true);
        return pane;
    }

    private TitledPane buildPublishersSection() {
        List<Game> games = GameDataService.getInstance().getAllGames();
        Map<String, List<Game>> byPublisher = games.stream()
                .sorted(Comparator.comparing(Game::getPublisherName))
                .collect(Collectors.groupingBy(Game::getPublisherName, LinkedHashMap::new, Collectors.toList()));

        VBox box = new VBox(12);
        for (Map.Entry<String, List<Game>> entry : byPublisher.entrySet()) {
            String publisher = entry.getKey();
            List<Game> pubGames = entry.getValue();

            VBox pubBox = new VBox(6);
            pubBox.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-padding: 10;");

            HBox header = new HBox(10);
            Label title = styledLabel(publisher, 15, true);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label count = styledLabel(pubGames.size() + " jeu(x)", 12, false);
            header.getChildren().addAll(title, spacer, count);

            VBox list = new VBox(4);
            for (Game g : pubGames) {
                Label line = styledLabel("• " + g.getName() + " — " + g.getSupportedPlatformsLabel(), 13, false);
                list.getChildren().add(line);
            }

            pubBox.getChildren().addAll(header, list);
            box.getChildren().add(pubBox);
        }

        TitledPane pane = new TitledPane("Pages éditeurs", box);
        pane.setExpanded(true);
        return pane;
    }

    private Label styledLabel(String text, int size, boolean bold) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: white; -fx-font-size: " + size + "px;" + (bold ? " -fx-font-weight: bold;" : ""));
        return l;
    }

    private String formatEuro(double value) {
        return String.format("%.2f €", value);
    }
}

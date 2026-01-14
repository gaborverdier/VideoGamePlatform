package org.example.old;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;


public class App extends Application {

    private String userRole = "PLAYER"; 

    @Override
    public void start(Stage stage) {
        // Dialogue de connexion
        if (!showLoginDialog()) {
            return;
        }

        // Construction des onglets
        TabPane mainLayout = new TabPane();
        mainLayout.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Onglet Magasin avec grille de jeux
        ScrollPane storeView = createStoreView();
        mainLayout.getTabs().add(new Tab("Magasin", storeView));
        
        // Onglet Bibliothèque
        mainLayout.getTabs().add(new Tab("Bibliothèque", new Label("   Mes jeux...")));

        // Onglet Admin uniquement
        if ("ADMIN".equals(userRole)) {
            TextArea console = new TextArea("> Initialisation Kafka...\n");
            console.setStyle("-fx-control-inner-background: black; -fx-text-fill: lime;");
            mainLayout.getTabs().add(new Tab("Console Admin", console));
        }

        // Onglet Editeur uniquement
        if ("EDITOR".equals(userRole)) {
            TextArea console = new TextArea("> Mes jeux publiés...\n");
            mainLayout.getTabs().add(new Tab("Mes jeux publiés", console));
        }

        Scene scene = new Scene(mainLayout, 800, 600);
        stage.setTitle("Projet JVM - " + userRole);
        stage.setScene(scene);
        stage.show();
    }

    // Petite boite de dialogue pour choisir qui on est
    private boolean showLoginDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText("Connexion au simulateur");

        ButtonType loginBtn = new ButtonType("Go", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginBtn, ButtonType.CANCEL);

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("PLAYER", "EDITOR", "ADMIN");
        roleBox.setValue("ADMIN");

        VBox content = new VBox(10, new Label("Rôle :"), roleBox);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == loginBtn) {
            this.userRole = roleBox.getValue();
            return true;
        }
        return false;
    }

    // Créer la vue du magasin avec des jeux mock
    private ScrollPane createStoreView() {
        List<Game> games = List.of(
            new Game("Elden Ring", 59.99, "RPG", new Image("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/322170/header.jpg?t=1703006148", true)),
            new Game("Minecraft", 29.99, "Sandbox", new Image("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/322170/header.jpg?t=1703006148", true)),
            new Game("FIFA 24", 69.99, "Sport", new Image("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/322170/header.jpg?t=1703006148", true)),
            new Game("The Witcher 3", 39.99, "RPG", new Image("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/322170/header.jpg?t=1703006148", true)),
            new Game("GTA V", 29.99, "Action", new Image("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/322170/header.jpg?t=1703006148", true)),
            new Game("Cyberpunk 2077", 49.99, "RPG", new Image("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/322170/header.jpg?t=1703006148", true)),
            new Game("CS:GO", 0.00, "FPS", new Image("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/322170/header.jpg?t=1703006148", true)),
            new Game("Valorant", 0.00, "FPS", new Image("https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/322170/header.jpg?t=1703006148", true))
        );
        
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(15);
        flowPane.setVgap(15);
        flowPane.setPadding(new Insets(20));
        
        for (Game game : games) {
            VBox gameCard = createGameCard(game);
            flowPane.getChildren().add(gameCard);
        }
        
        ScrollPane scrollPane = new ScrollPane(flowPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #2b2b2b;");
        
        return scrollPane;
    }
    
    // Créer une carte de jeu avec image
    private VBox createGameCard(Game game) {
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
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Genre
        Label genreLabel = new Label(game.getGenre());
        genreLabel.setStyle("-fx-text-fill: #aaa;");
        
        // Prix
        Label priceLabel = new Label(game.getPrice() == 0 ? "GRATUIT" : String.format("%.2f€", game.getPrice()));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        card.getChildren().addAll(imageView, nameLabel, genreLabel, priceLabel);
        
        // Effet hover
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 5; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5; -fx-cursor: hand;"));
        
        // Click pour acheter
        card.setOnMouseClicked(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Acheter un jeu");
            alert.setHeaderText(game.getName());
            alert.setContentText(String.format("Prix: %.2f€\nVoulez-vous acheter ce jeu ?", game.getPrice()));
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Achat réussi");
                    success.setContentText("Le jeu a été ajouté à votre bibliothèque !");
                    success.showAndWait();
                }
            });
        });
        
        return card;
    }

    public static void main(String[] args) {
        launch();
    }
}
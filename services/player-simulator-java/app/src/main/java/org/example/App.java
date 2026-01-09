package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

import org.Game;

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

        // Onglet Magasin avec liste de jeux
        ListView<Game> storeList = createStoreView();
        mainLayout.getTabs().add(new Tab("Magasin", storeList));
        
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
    private ListView<Game> createStoreView() {
        List<Game> games = List.of(
            new Game("Elden Ring", 59.99, "RPG"),
            new Game("Minecraft", 29.99, "Sandbox"),
            new Game("FIFA 24", 69.99, "Sport"),
            new Game("The Witcher 3", 39.99, "RPG"),
            new Game("GTA V", 29.99, "Action"),
            new Game("Cyberpunk 2077", 49.99, "RPG"),
            new Game("CS:GO", 0.00, "FPS"),
            new Game("Valorant", 0.00, "FPS")
        );
        
        ObservableList<Game> gameList = FXCollections.observableArrayList(games);
        ListView<Game> listView = new ListView<>(gameList);
        
        // Gérer le double-clic pour acheter un jeu
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Game selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Achat");
                    alert.setHeaderText("Jeu acheté !");
                    alert.setContentText("Vous avez acheté : " + selected.getName());
                    alert.showAndWait();
                }
            }
        });
        
        return listView;
    }

    public static void main(String[] args) {
        launch();
    }
}
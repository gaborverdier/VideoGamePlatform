package com.views.components.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Arrays;
import java.util.List;

public class PublishGameDialog {
    
    public static PublishedGameData show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Publier un jeu");
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("Publier un nouveau jeu");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: #3c3c3c;");
        
        // Champs du formulaire
        Label nameLabel = new Label("Nom du jeu:");
        nameLabel.setStyle("-fx-text-fill: white;");
        TextField nameField = new TextField();
        nameField.setPromptText("Ex: The Last Shadow");
        nameField.setPrefWidth(300);
        
        Label platformLabel = new Label("Plateforme:");
        platformLabel.setStyle("-fx-text-fill: white;");
        ComboBox<String> platformCombo = new ComboBox<>();
        platformCombo.getItems().addAll("PC", "PS5", "XBOX", "Nintendo Switch", "Multiple");
        platformCombo.setPrefWidth(300);
        
        Label genresLabel = new Label("Genres (séparés par virgule):");
        genresLabel.setStyle("-fx-text-fill: white;");
        TextArea genresArea = new TextArea();
        genresArea.setPromptText("Ex: Simulation, Stratégie");
        genresArea.setPrefHeight(60);
        genresArea.setWrapText(true);
        
        Label versionLabel = new Label("Version initiale:");
        versionLabel.setStyle("-fx-text-fill: white;");
        TextField versionField = new TextField();
        versionField.setText("1.0");
        versionField.setPrefWidth(300);
        
        Label priceLabel = new Label("Prix (€):");
        priceLabel.setStyle("-fx-text-fill: white;");
        TextField priceField = new TextField();
        priceField.setPromptText("Ex: 19.99");
        priceField.setPrefWidth(300);
        
        Label descriptionLabel = new Label("Description:");
        descriptionLabel.setStyle("-fx-text-fill: white;");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Décrivez votre jeu...");
        descriptionArea.setPrefHeight(80);
        descriptionArea.setWrapText(true);
        
        Label imageLabel = new Label("Image de couverture (chemin):");
        imageLabel.setStyle("-fx-text-fill: white;");
        TextField imageField = new TextField();
        imageField.setPromptText("Ex: /images/game-cover.png");
        imageField.setPrefWidth(300);
        
        // Ajout des champs au formulaire
        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);
        form.add(platformLabel, 0, 1);
        form.add(platformCombo, 1, 1);
        form.add(genresLabel, 0, 2);
        form.add(genresArea, 1, 2);
        form.add(versionLabel, 0, 3);
        form.add(versionField, 1, 3);
        form.add(priceLabel, 0, 4);
        form.add(priceField, 1, 4);
        form.add(descriptionLabel, 0, 5);
        form.add(descriptionArea, 1, 5);
        form.add(imageLabel, 0, 6);
        form.add(imageField, 1, 6);
        
        scrollPane.setContent(form);
        
        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        
        Button publishButton = new Button("Publier");
        publishButton.setPrefWidth(100);
        publishButton.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        
        Button cancelButton = new Button("Annuler");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        
        buttonBox.getChildren().addAll(publishButton, cancelButton);
        
        root.getChildren().addAll(titleLabel, new Separator(), scrollPane, buttonBox);
        
        Scene scene = new Scene(root, 600, 700);
        dialog.setScene(scene);
        
        PublishedGameData[] result = {null};
        
        publishButton.setOnAction(e -> {
            if (!nameField.getText().isEmpty() && platformCombo.getValue() != null && 
                !priceField.getText().isEmpty()) {
                try {
                    double price = Double.parseDouble(priceField.getText());
                    List<String> genres = Arrays.asList(genresArea.getText().split(","));
                    genres.replaceAll(String::trim);
                    
                    result[0] = new PublishedGameData(
                        nameField.getText(),
                        platformCombo.getValue(),
                        genres,
                        versionField.getText(),
                        price,
                        imageField.getText(),
                        descriptionArea.getText()
                    );
                    dialog.close();
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setContentText("Le prix doit être un nombre valide");
                    alert.showAndWait();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Champs manquants");
                alert.setContentText("Veuillez remplir tous les champs obligatoires");
                alert.showAndWait();
            }
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        dialog.showAndWait();
        return result[0];
    }
    
    public static class PublishedGameData {
        public String name;
        public String platform;
        public List<String> genres;
        public String version;
        public double price;
        public String coverImagePath;
        public String description;
        
        public PublishedGameData(String name, String platform, List<String> genres,
                                String version, double price, String coverImagePath, String description) {
            this.name = name;
            this.platform = platform;
            this.genres = genres;
            this.version = version;
            this.price = price;
            this.coverImagePath = coverImagePath;
            this.description = description;
        }
    }
}

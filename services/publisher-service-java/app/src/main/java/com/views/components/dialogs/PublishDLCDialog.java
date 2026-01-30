package com.views.components.dialogs;

import com.gaming.api.models.DLCModel;
import com.util.AvroJacksonConfig;
import com.util.ApiClient;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PublishDLCDialog {
    
    public static DLCData show(String gameId) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Publier un DLC");
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("Publier un DLC");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: #3c3c3c;");
        
        Label nameLabel = new Label("Nom du DLC:");
        nameLabel.setStyle("-fx-text-fill: white;");
        TextField nameField = new TextField();
        nameField.setPromptText("Ex: Expansion Arctic");
        nameField.setPrefWidth(300);
        
        Label priceLabel = new Label("Prix (€):");
        priceLabel.setStyle("-fx-text-fill: white;");
        TextField priceField = new TextField();
        priceField.setPromptText("Ex: 9.99");
        priceField.setPrefWidth(300);
        
        Label descriptionLabel = new Label("Description:");
        descriptionLabel.setStyle("-fx-text-fill: white;");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Décrivez le contenu du DLC...");
        descriptionArea.setPrefHeight(80);
        descriptionArea.setWrapText(true);
        
        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);
        form.add(priceLabel, 0, 1);
        form.add(priceField, 1, 1);
        form.add(descriptionLabel, 0, 2);
        form.add(descriptionArea, 1, 2);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(form);
        scrollPane.setFitToWidth(true);
        
        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        
        Button publishButton = new Button("Publier le DLC");
        publishButton.setPrefWidth(120);
        publishButton.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        
        Button cancelButton = new Button("Annuler");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        
        buttonBox.getChildren().addAll(publishButton, cancelButton);
        
        root.getChildren().addAll(titleLabel, new Separator(), scrollPane, buttonBox);
        
        Scene scene = new Scene(root, 600, 400);
        dialog.setScene(scene);
        
        DLCData[] result = {null};
        
        publishButton.setOnAction(e -> {
            if (!nameField.getText().isEmpty() && !priceField.getText().isEmpty()) {
                try {
                    double price = Double.parseDouble(priceField.getText());
                    
                    DLCModel dlcModel = new DLCModel();
                    dlcModel.setTitle(nameField.getText());
                    dlcModel.setPrice(price);
                    dlcModel.setDescription(descriptionArea.getText());
                    dlcModel.setGameId(gameId);
                    dlcModel.setReleaseTimeStamp(System.currentTimeMillis());

                    String json = AvroJacksonConfig.avroObjectMapper().writeValueAsString(dlcModel);
                    String responseJson = ApiClient.postJson("/api/dlc/create", json);
                    System.out.println("Response JSON: " + responseJson);

                    DLCModel created = AvroJacksonConfig.avroObjectMapper()
                        .readValue(responseJson, DLCModel.class);

                    result[0] = new DLCData(
                        nameField.getText(),
                        price,
                        descriptionArea.getText()
                    );
                    dialog.close();
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setContentText("Le prix doit être un nombre valide");
                    alert.showAndWait();
                } catch (Exception ex) {
                    System.out.println("[DLC] Error during DLC publish:");
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setContentText("Erreur lors de la publication du DLC: " + ex.getMessage());
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
    
    public static class DLCData {
        public String name;
        public double price;
        public String description;
        
        public DLCData(String name, double price, String description) {
            this.name = name;
            this.price = price;
            this.description = description;
        }
    }
}

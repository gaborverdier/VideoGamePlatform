package com.views.components.dialogs;

import com.gaming.api.models.PatchModel;
import com.util.ApiClient;
import com.util.AvroJacksonConfig;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Arrays;
import java.util.List;

public class PublishPatchDialog {
    
    public static PatchData show(String gameId) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Publier une mise à jour (Patch)");
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("Publier une mise à jour");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));
        form.setStyle("-fx-background-color: #3c3c3c;");
        
        Label versionLabel = new Label("Numéro de version:");
        versionLabel.setStyle("-fx-text-fill: white;");
        TextField versionField = new TextField();
        versionField.setPromptText("Ex: 1.0.1");
        versionField.setPrefWidth(300);
        
        Label commentLabel = new Label("Commentaire:");
        commentLabel.setStyle("-fx-text-fill: white;");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Décrivez les modifications principales...");
        commentArea.setPrefHeight(80);
        commentArea.setWrapText(true);
        
        Label modificationsLabel = new Label("Liste des modifications (une par ligne):");
        modificationsLabel.setStyle("-fx-text-fill: white;");
        TextArea modificationsArea = new TextArea();
        modificationsArea.setPromptText("Ex:\nFix bug inventory\nOptimisation performance\nNouvelles quêtes");
        modificationsArea.setPrefHeight(120);
        modificationsArea.setWrapText(true);
        
        form.add(versionLabel, 0, 0);
        form.add(versionField, 1, 0);
        form.add(commentLabel, 0, 1);
        form.add(commentArea, 1, 1);
        form.add(modificationsLabel, 0, 2);
        form.add(modificationsArea, 1, 2);
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(form);
        scrollPane.setFitToWidth(true);
        
        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        
        Button publishButton = new Button("Publier le patch");
        publishButton.setPrefWidth(120);
        publishButton.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        
        Button cancelButton = new Button("Annuler");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        
        buttonBox.getChildren().addAll(publishButton, cancelButton);
        
        root.getChildren().addAll(titleLabel, new Separator(), scrollPane, buttonBox);
        
        Scene scene = new Scene(root, 600, 600);
        dialog.setScene(scene);
        
        PatchData[] result = {null};
        
        publishButton.setOnAction(e -> {
            if (!versionField.getText().isEmpty() && !modificationsArea.getText().isEmpty()) {
                List<String> modifications = Arrays.asList(modificationsArea.getText().split("\n"));
                modifications.replaceAll(String::trim);
                modifications.removeIf(String::isEmpty);

                try {
                    PatchModel patchModel = new PatchModel();
                    patchModel.setId("");
                    patchModel.setGameId(gameId);
                    patchModel.setVersion(versionField.getText());
                    patchModel.setReleaseTimeStamp(System.currentTimeMillis());
                    patchModel.setDescription(commentArea.getText());

                    String json = AvroJacksonConfig.avroObjectMapper().writeValueAsString(patchModel);
                    String responseJson = ApiClient.postJson("/api/patch/create", json);
                    System.out.println("Response JSON: " + responseJson);

                    PatchModel created = AvroJacksonConfig.avroObjectMapper()
                        .readValue(responseJson, PatchModel.class);

                    result[0] = new PatchData(
                        created.getVersion(),
                        created.getDescription(),
                        modifications
                    );
                    dialog.close();
                } catch (Exception ex) {
                    System.out.println("[PATCH] Error during patch publish:");
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setContentText("Erreur lors de la publication du patch: " + ex.getMessage());
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
    
    public static class PatchData {
        public String version;
        public String comment;
        public List<String> modifications;
        
        public PatchData(String version, String comment, List<String> modifications) {
            this.version = version;
            this.comment = comment;
            this.modifications = modifications;
        }
    }
}

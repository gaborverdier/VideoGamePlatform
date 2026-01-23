package org.example.views.components.dialogs;

import org.example.controllers.PlayerDashboardController;
import org.example.models.Game;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GamePlayDialog {
    
    public static void show(Game game, Runnable onUpdate, PlayerDashboardController controller) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("En jeu : " + game.getName());
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1a1a1a;");
        
        Label titleLabel = new Label("🎮 " + game.getName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label timeLabel = new Label("Temps de jeu: " + game.getPlayedTime() + " min");
        timeLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #aaa;");
        
        // Boutons +/- temps de jeu
        HBox timeBox = new HBox(10);
        timeBox.setAlignment(Pos.CENTER);
        
        Button minusBtn = new Button("- 10 min");
        minusBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
        minusBtn.setOnAction(e -> {
            game.addPlayedTime(-10);
            timeLabel.setText("Temps de jeu: " + game.getPlayedTime() + " min");
        });
        
        Button plusBtn = new Button("+ 10 min");
        plusBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        plusBtn.setOnAction(e -> {
            game.addPlayedTime(10);
            timeLabel.setText("Temps de jeu: " + game.getPlayedTime() + " min");
        });
        
        timeBox.getChildren().addAll(minusBtn, plusBtn);
        
        // Crash options: choose a code and optional message
        ChoiceBox<String> crashType = new ChoiceBox<>();
        crashType.getItems().addAll("1 - Graphics", "2 - Network", "3 - Input", "99 - Unknown");
        crashType.setValue("99 - Unknown");

        TextField crashMessageField = new TextField();
        crashMessageField.setPromptText("Détails du crash (optionnel)");

        Button crashBtn = new Button("CRASH LE JEU");
        crashBtn.setStyle("-fx-background-color: #ff5722; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        crashBtn.setOnAction(e -> {
            String sel = crashType.getValue();
            int code = 99;
            try {
                code = Integer.parseInt(sel.split(" ")[0]);
            } catch (Exception ex) { }

            String details = crashMessageField.getText();
            if (controller != null) {
                controller.reportCrash(game.getId(), "1.0", code, details);
            }

            Alert crash = new Alert(Alert.AlertType.ERROR);
            crash.setTitle("Crash !");
            crash.setHeaderText("Le jeu a planté !");
            crash.setContentText("Rapport de crash généré.\n(Sera envoyé via Kafka)");
            crash.showAndWait();
            dialog.close();
        });
        
        Button quitBtn = new Button("Quitter le jeu");
        quitBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white;");
        quitBtn.setOnAction(e -> {
            if (onUpdate != null) onUpdate.run();
            dialog.close();
        });
        
        HBox crashBox = new HBox(10, crashType, crashMessageField);
        crashBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(titleLabel, timeLabel, timeBox, new Separator(), crashBox, crashBtn, quitBtn);
        
        Scene scene = new Scene(root, 400, 350);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

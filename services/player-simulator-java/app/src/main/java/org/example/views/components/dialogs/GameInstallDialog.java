package org.example.views.components.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.models.Game;

public class GameInstallDialog {
    
    public static void show(Game game, Runnable onInstalled) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Installation - " + game.getName());
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("Installation de " + game.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        
        Label statusLabel = new Label("Téléchargement en cours...");
        statusLabel.setStyle("-fx-text-fill: #aaa;");
        
        root.getChildren().addAll(titleLabel, progressBar, statusLabel);
        
        Scene scene = new Scene(root, 400, 200);
        dialog.setScene(scene);
        dialog.show();
        
        new Thread(() -> {
            for (int i = 0; i <= 100; i++) {
                final int progress = i;
                Platform.runLater(() -> {
                    progressBar.setProgress(progress / 100.0);
                    statusLabel.setText("Installation : " + progress + "%");
                });
                try {
                    Thread.sleep(50); // 5 secondes au total
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            Platform.runLater(() -> {
                game.setInstalled(true);
                dialog.close();
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Installation terminée");
                success.setContentText(game.getName() + " est maintenant installé !");
                success.showAndWait();
                
                if (onInstalled != null) {
                    onInstalled.run();
                }
            });
        }).start();
    }
}

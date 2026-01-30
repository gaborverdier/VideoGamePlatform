package org.example.views.components.dialogs;

import org.example.models.Game;
import org.example.services.GameDataService;
import org.example.services.SessionManager;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            Platform.runLater(() -> {
                dialog.close();
                
                // Notify backend that the game was installed for the current user
                try {
                    String userId = SessionManager.getInstance().getCurrentPlayer() != null
                            ? SessionManager.getInstance().getCurrentPlayer().getId()
                            : null;
                    System.out.println("Current user ID: " + userId);
                    if (userId != null) {
                        GameDataService.getInstance().installGameForUser(userId, game.getId(), game.getVersion());
                        game.setInstalled(true);
                        game.setInstalledVersion(game.getVersion());
                        System.out.println("Game " + game.getName() + " installed for user " + userId + " version=" + game.getVersion());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

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

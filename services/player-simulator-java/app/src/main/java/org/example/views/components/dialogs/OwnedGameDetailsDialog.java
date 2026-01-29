package org.example.views.components.dialogs;

import org.example.models.Game;
import org.example.services.SessionManager;
import org.example.services.GameDataService;
import org.example.services.PlatformApiClient;
import com.gaming.api.models.DLCModel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class OwnedGameDetailsDialog {
    
    public static void show(Game game, Runnable onUpdate) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(game.getName());
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        // GAUCHE: Image
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(20));
        leftPane.setAlignment(Pos.TOP_CENTER);
        
        ImageView imageView = new ImageView(game.getCoverImage());
        imageView.setFitWidth(300);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        
        leftPane.getChildren().add(imageView);
        
        // CENTRE: Détails
        VBox centerPane = new VBox(15);
        centerPane.setPadding(new Insets(20));
        
        Label titleLabel = new Label(game.getName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label platformLabel = new Label("Support acheté: " + (game.getOwnedPlatforms().isEmpty() ? "N/A" : game.getOwnedPlatformsLabel()));
        platformLabel.setStyle("-fx-text-fill: #aaa;");
        Label supportedLabel = new Label("Supports disponibles: " + game.getSupportedPlatformsLabel());
        supportedLabel.setStyle("-fx-text-fill: #aaa;");
        
        Label statusLabel = new Label(game.isInstalled() ? "✅ Installé" : "⬇ Pas encore installé");
        statusLabel.setStyle("-fx-text-fill: " + (game.isInstalled() ? "#4CAF50" : "#FF9800") + "; -fx-font-size: 14px;");
        
        Label timeLabel = new Label("Temps de jeu: " + game.getPlayedTime() + " min");
        timeLabel.setStyle("-fx-text-fill: #aaa;");

        // Fetch total playtime from platform server (non-blocking)
        new Thread(() -> {
            try {
                PlatformApiClient api = new PlatformApiClient();
                long totalMs = api.getTotalPlayedForGameAllTime(game.getId());
                long totalMin = totalMs / 60_000L;
                javafx.application.Platform.runLater(() -> {
                    timeLabel.setText("Temps total de jeu: " + totalMin + " min ");
                });
            } catch (Exception ex) {
                // best-effort: ignore failures
            }
        }).start();
        
        Label backendVerLabel = new Label("Version serveur: " + (game.getVersion() != null ? game.getVersion() : "N/A"));
        backendVerLabel.setStyle("-fx-text-fill: #aaa;");

        Label installedVerLabel = new Label("Version installée: " + (game.getInstalledVersion() != null ? game.getInstalledVersion() : "N/A"));
        installedVerLabel.setStyle("-fx-text-fill: #aaa;");
        
        // Boutons d'action
        VBox actionsBox = new VBox(10);

        // create buttons first so handlers can reference each other
        Button playBtn = new Button(game.isInstalled() ? "▶ JOUER" : "⬇ INSTALLER");
        playBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        playBtn.setMaxWidth(Double.MAX_VALUE);

        Button updateNowBtn = new Button("🔁 Mettre à jour");
        updateNowBtn.setMaxWidth(Double.MAX_VALUE);
        boolean needsUpdate = game.isInstalled() && game.getVersion() != null && (game.getInstalledVersion() == null || !game.getVersion().equals(game.getInstalledVersion()));
        if (needsUpdate) {
            updateNowBtn.setDisable(false);
            updateNowBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-weight: bold;");
            updateNowBtn.setText("🔁 Mettre à jour → " + (game.getVersion() != null ? game.getVersion() : "N/A"));
        } else {
            updateNowBtn.setDisable(true);
            updateNowBtn.setStyle("");
            updateNowBtn.setText("🔁 Mettre à jour");
        }

        // now assign handlers
        playBtn.setOnAction(e -> {
            if (game.isInstalled()) {
                GamePlayDialog.show(game, onUpdate, SessionManager.getInstance().getPlayerController());
            } else {
                GameInstallDialog.show(game, () -> {
                    statusLabel.setText("✅ Installé");
                    statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px;");
                    playBtn.setText("▶ JOUER");
                    // update installed version label and update button state
                    installedVerLabel.setText("Version installée: " + (game.getInstalledVersion() != null ? game.getInstalledVersion() : "N/A"));
                    boolean nowNeedsUpdate = game.isInstalled() && game.getVersion() != null && (game.getInstalledVersion() == null || !game.getVersion().equals(game.getInstalledVersion()));
                    if (nowNeedsUpdate) {
                        updateNowBtn.setDisable(false);
                        updateNowBtn.setStyle("-fx-background-color: #e53935; -fx-text-fill: white; -fx-font-weight: bold;");
                        updateNowBtn.setText("🔁 Mettre à jour → " + (game.getVersion() != null ? game.getVersion() : "N/A"));
                    } else {
                        updateNowBtn.setDisable(true);
                        updateNowBtn.setStyle("");
                        updateNowBtn.setText("🔁 Mettre à jour");
                    }
                    if (onUpdate != null) onUpdate.run();
                });
            }
        });

        Button reviewBtn = new Button("⭐ Laisser un avis");
        reviewBtn.setMaxWidth(Double.MAX_VALUE);
        reviewBtn.setOnAction(e -> ReviewDialog.show(game));

        Button seeReviewsBtn = new Button("Voir les avis (" + game.getReviews().size() + ")");
        seeReviewsBtn.setMaxWidth(Double.MAX_VALUE);
        seeReviewsBtn.setOnAction(e -> ReviewsListDialog.show(game));

        Button favoriteBtn = new Button(game.isFavorite() ? "❤ Retirer des favoris" : "❤ Ajouter aux favoris");
        favoriteBtn.setMaxWidth(Double.MAX_VALUE);
        favoriteBtn.setOnAction(e -> {
            game.setFavorite(!game.isFavorite());
            favoriteBtn.setText(game.isFavorite() ? "❤ Retirer des favoris" : "❤ Ajouter aux favoris");
            if (onUpdate != null) onUpdate.run();
        });

        updateNowBtn.setOnAction(e -> {
            String userId = SessionManager.getInstance().getCurrentPlayer() != null ? SessionManager.getInstance().getCurrentPlayer().getId() : null;
            if (userId == null) return;
            try {
                GameDataService.getInstance().installGameForUser(userId, game.getId(), game.getVersion());
                game.setInstalled(true);
                game.setInstalledVersion(game.getVersion());
                updateNowBtn.setDisable(true);
                updateNowBtn.setStyle("");
                updateNowBtn.setText("🔁 Mettre à jour");
                statusLabel.setText("✅ Installé");
                statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px;");
                playBtn.setText("▶ JOUER");
                installedVerLabel.setText("Version installée: " + (game.getInstalledVersion() != null ? game.getInstalledVersion() : "N/A"));
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setContentText("Jeu mis à jour vers la version " + game.getVersion());
                info.showAndWait();
                if (onUpdate != null) onUpdate.run();
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setContentText("Échec de la mise à jour : " + ex.getMessage());
                err.showAndWait();
            }
        });
        
        // Populate remote DLCs (best-effort) so the DLC button state is correct
        try {
            java.util.List<DLCModel> remote = GameDataService.getInstance().getDLCsForGame(game.getId());
            if (remote != null) {
                for (DLCModel dm : remote) {
                    // add if not already present (by title)
                    boolean exists = game.getAvailableDLCs().stream().anyMatch(d -> d.getName().equals(dm.getTitle()));
                    if (!exists) {
                        game.addDLC(dm.getTitle(), 0.0);
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Failed to fetch DLCs for owned game " + game.getId() + ": " + ex.getMessage());
        }

        // DLCs
        int totalDLCs = game.getAvailableDLCs().size();
        Button dlcBtn = new Button("🎁 DLCs (" + totalDLCs + ")");
        dlcBtn.setMaxWidth(Double.MAX_VALUE);
        dlcBtn.setDisable(totalDLCs == 0);
        dlcBtn.setOnAction(e -> showDLCsDialog(game, dlcBtn, onUpdate));
        
        actionsBox.getChildren().addAll(playBtn, reviewBtn, seeReviewsBtn, favoriteBtn, updateNowBtn, dlcBtn);
        
        centerPane.getChildren().addAll(titleLabel, platformLabel, supportedLabel, statusLabel, timeLabel, new Separator(), actionsBox);
        
        // BAS: Bouton fermer
        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(20));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button closeBtn = new Button("Fermer");
        closeBtn.setOnAction(e -> dialog.close());
        buttonBox.getChildren().add(closeBtn);
        
        root.setLeft(leftPane);
        root.setCenter(centerPane);
        root.setBottom(buttonBox);
        
        Scene scene = new Scene(root, 600, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private static void showUpdatesDialog(Game game, Button updateBtn, Runnable onUpdate) {
        if (game.getPendingUpdates().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Aucune mise à jour disponible.");
            alert.showAndWait();
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Mises à jour disponibles");
        alert.setHeaderText(game.getName());
        alert.setContentText("Installer :\n" + String.join("\n", game.getPendingUpdates()));
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                for (String update : new java.util.ArrayList<>(game.getPendingUpdates())) {
                    game.installUpdate(update);
                }
                updateBtn.setText("⬇ Télécharger MAJ (0)");
                updateBtn.setDisable(true);
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setContentText("Mises à jour installées !");
                success.showAndWait();
                
                if (onUpdate != null) onUpdate.run();
            }
        });
    }
    
    private static void showDLCsDialog(Game game, Button dlcBtn, Runnable onUpdate) {
        if (game.getPendingDLCs().isEmpty() && game.getAvailableDLCs().stream().noneMatch(Game.DLC::isInstalled)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Aucun DLC disponible.");
            alert.showAndWait();
            return;
        }
        
        // Créer une dialog personnalisée pour choisir les DLCs
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("DLCs - " + game.getName());
        
        VBox root = new VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("DLCs pour " + game.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");
        
        VBox dlcList = new VBox(10);
        dlcList.setPadding(new javafx.geometry.Insets(5));
        
        // Afficher tous les DLCs (installés et non installés)
        for (Game.DLC dlc : game.getAvailableDLCs()) {
            VBox dlcItem = new VBox(5);
            dlcItem.setPadding(new javafx.geometry.Insets(10));
            dlcItem.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
            
            HBox headerRow = new HBox(10);
            headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Label nameLabel = new Label(dlc.getName());
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            Label statusLabel = new Label(dlc.isInstalled() ? "✅ Installé" : "⬇ Non installé");
            statusLabel.setStyle("-fx-text-fill: " + (dlc.isInstalled() ? "#4CAF50" : "#FF9800") + ";");
            
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            Label priceLabel = new Label(dlc.getFormattedPrice());
            priceLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14px;");
            
            headerRow.getChildren().addAll(nameLabel, statusLabel, spacer, priceLabel);
            
            // Infos supplémentaires pour DLC installé
            HBox infoRow = new HBox(15);
            infoRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            if (dlc.isInstalled()) {
                Label timeLabel = new Label("⏱ " + dlc.getPlayedTime() + " min jouées");
                timeLabel.setStyle("-fx-text-fill: #aaa;");
                
                infoRow.getChildren().addAll(timeLabel);
            }

            // Boutons d'action
            HBox actionsRow = new HBox(10);
            actionsRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            if (dlc.isInstalled()) {
                Button playDlcBtn = new Button("▶ Jouer");
                playDlcBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                playDlcBtn.setOnAction(e -> {
                    // Simuler 15 min de jeu sur le DLC
                    dlc.addPlayedTime(15);
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setContentText("Vous avez joué 15 minutes au DLC " + dlc.getName() + " !");
                    info.showAndWait();
                });
                
                Button reviewDlcBtn = new Button("⭐ Évaluer");
                reviewDlcBtn.setOnAction(e -> ReviewDialog.showForDLC(dlc));
                
                actionsRow.getChildren().addAll(playDlcBtn, reviewDlcBtn);
            } else {
                // Acheter le DLC
                Button buyBtn = new Button("Acheter");
                buyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                buyBtn.setOnAction(e -> {
                    if (SessionManager.getInstance().getCurrentPlayer().getWallet() < dlc.getPrice()) {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setContentText("Solde insuffisant !");
                        error.showAndWait();
                    } else {
                        SessionManager.getInstance().getCurrentPlayer()
                            .setWallet(SessionManager.getInstance().getCurrentPlayer().getWallet() - dlc.getPrice());
                        
                        game.installDLC(dlc);
                        
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setContentText("DLC acheté et installé !");
                        success.showAndWait();
                        
                        dialog.close();
                        
                        dlcBtn.setText("🎁 DLCs (" + game.getAvailableDLCs().size() + ")");
                        
                        if (onUpdate != null) onUpdate.run();
                    }
                });
                actionsRow.getChildren().addAll(buyBtn);
            }
            
            // Avis
            Button seeReviewsDlcBtn = new Button("Avis (" + dlc.getReviews().size() + ")");
            seeReviewsDlcBtn.setOnAction(e -> ReviewsListDialog.showForDLC(dlc));

            double avgRating = dlc.getAverageRating();
            Label ratingLabel = new Label(avgRating > 0 ? String.format("⭐ %.1f/5", avgRating) : "Pas de note");
            ratingLabel.setStyle("-fx-text-fill: #FFD700;");
            actionsRow.getChildren().addAll(seeReviewsDlcBtn, ratingLabel);
            
            dlcItem.getChildren().addAll(headerRow);
            if (!infoRow.getChildren().isEmpty()) {
                dlcItem.getChildren().add(infoRow);
            }
            dlcItem.getChildren().add(actionsRow);
            dlcList.getChildren().add(dlcItem);
        }
        
        scrollPane.setContent(dlcList);
        
        Button closeBtn = new Button("Fermer");
        closeBtn.setOnAction(e -> dialog.close());
        
        root.getChildren().addAll(titleLabel, new javafx.scene.control.Separator(), scrollPane, closeBtn);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 550, 450);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

package org.example.views.components.dialogs;

import org.example.models.Game;
import org.example.services.GameDataService;
import org.example.services.PlatformApiClient;
import org.example.services.SessionManager;

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

        Label platformLabel = new Label("Support: " + game.getPlatform());
        platformLabel.setStyle("-fx-text-fill: #aaa;");
        
        Label statusLabel = new Label(game.isInstalled() ? "✅ Installé" : "⬇ Pas encore installé");
        statusLabel.setStyle("-fx-text-fill: " + (game.isInstalled() ? "#4CAF50" : "#FF9800") + "; -fx-font-size: 14px;");
        
        Label timeLabel = new Label("Temps de jeu: " + game.getPlayedTime() + " min");
        timeLabel.setStyle("-fx-text-fill: #aaa;");

        // Fetch total playtime from platform server for current user only (non-blocking)
        new Thread(() -> {
            try {
                String userId = SessionManager.getInstance().getCurrentPlayer() != null ? SessionManager.getInstance().getCurrentPlayer().getId() : null;
                if (userId != null) {
                    PlatformApiClient api = new PlatformApiClient();
                    long totalMs = api.getTotalPlayedForUser(game.getId(), userId);
                    long totalMin = totalMs / 60_000L;
                    // cache the backend total on the Game instance so other dialogs can reuse it
                    game.setTotalPlayedAllTimeMs(totalMs);
                    javafx.application.Platform.runLater(() -> timeLabel.setText("Temps total de jeu: " + totalMin + " min "));
                }
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

        // Open the updates dialog when clicking the update button
        updateNowBtn.setOnAction(e -> showUpdatesDialog(game, updateNowBtn, onUpdate));

        // refresh playtime from platform server for current user (used after closing GamePlayDialog)
        Runnable refreshPlaytime = () -> {
            new Thread(() -> {
                try {
                    String userId = SessionManager.getInstance().getCurrentPlayer() != null ? SessionManager.getInstance().getCurrentPlayer().getId() : null;
                    if (userId != null) {
                        PlatformApiClient api = new PlatformApiClient();
                        long totalMs = api.getTotalPlayedForUser(game.getId(), userId);
                        long totalMin = totalMs / 60_000L;
                        game.setTotalPlayedAllTimeMs(totalMs);
                        javafx.application.Platform.runLater(() -> timeLabel.setText("Temps total de jeu: " + totalMin + " min "));
                    }
                } catch (Exception ex) {
                    // ignore
                }
            }).start();
        };

        // composite update: refresh this dialog and forward onUpdate to parent if present
        Runnable combinedOnUpdate = () -> {
            try { refreshPlaytime.run(); } catch (Exception ignore) {}
            if (onUpdate != null) onUpdate.run();
        };

        // now assign handlers
        playBtn.setOnAction(e -> {
            if (game.isInstalled()) {
                GamePlayDialog.show(game, combinedOnUpdate, SessionManager.getInstance().getPlayerController());
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

        Button seeReviewsBtn = new Button("Voir les avis (" + game.getReviews().size() + ")");
        seeReviewsBtn.setMaxWidth(Double.MAX_VALUE);
        seeReviewsBtn.setOnAction(e -> ReviewsListDialog.show(game));

        Button reviewBtn = new Button("⭐ Laisser un avis");
        reviewBtn.setMaxWidth(Double.MAX_VALUE);
        reviewBtn.setOnAction(e -> {
            ReviewDialog.show(game, () -> seeReviewsBtn.setText("Voir les avis (" + game.getReviews().size() + ")"));
        });

        Button favoriteBtn = new Button(game.isFavorite() ? "❤ Retirer des favoris" : "❤ Ajouter aux favoris");
        favoriteBtn.setMaxWidth(Double.MAX_VALUE);
        favoriteBtn.setOnAction(e -> {
            game.setFavorite(!game.isFavorite());
            favoriteBtn.setText(game.isFavorite() ? "❤ Retirer des favoris" : "❤ Ajouter aux favoris");
            if (onUpdate != null) onUpdate.run();
        });

        // DLCs button
        // Try to prefer backend-populated DLCs if available in GameDataService
        int totalDLCs = game.getAvailableDLCs().size();
        try {
            Game backendGame = GameDataService.getInstance().findGameById(game.getId());
            if (backendGame != null && backendGame.getAvailableDLCs() != null && !backendGame.getAvailableDLCs().isEmpty()) {
                game.getAvailableDLCs().clear();
                game.getAvailableDLCs().addAll(backendGame.getAvailableDLCs());
                totalDLCs = game.getAvailableDLCs().size();
            }
        } catch (Exception ignore) {}

        Button dlcBtn = new Button("🎁 DLCs (" + totalDLCs + ")");
        dlcBtn.setMaxWidth(Double.MAX_VALUE);
        dlcBtn.setDisable(totalDLCs == 0);

        // centralised refresh routine: reload backend data and update visible controls
        Runnable refreshFromBackend = () -> {
            new Thread(() -> {
                try {
                    GameDataService.getInstance().reload();
                    Game refreshed = GameDataService.getInstance().findGameById(game.getId());
                    if (refreshed != null) {
                        javafx.application.Platform.runLater(() -> {
                            try {
                                game.getReviews().clear();
                                game.getReviews().addAll(refreshed.getReviews());
                            } catch (Exception ignore) {}

                            // ensure DLC list is updated from backend copy
                            try {
                                game.getAvailableDLCs().clear();
                                if (refreshed.getAvailableDLCs() != null)
                                    game.getAvailableDLCs().addAll(refreshed.getAvailableDLCs());
                            } catch (Exception ignore) {}

                            timeLabel.setText("Temps de jeu: " + game.getPlayedTime() + " min");
                            installedVerLabel.setText("Version installée: " + (game.getInstalledVersion() != null ? game.getInstalledVersion() : "N/A"));
                            statusLabel.setText(game.isInstalled() ? "✅ Installé" : "⬇ Pas encore installé");
                            statusLabel.setStyle("-fx-text-fill: " + (game.isInstalled() ? "#4CAF50" : "#FF9800") + "; -fx-font-size: 14px;");
                            seeReviewsBtn.setText("Voir les avis (" + game.getReviews().size() + ")");
                            dlcBtn.setText("🎁 DLCs (" + game.getAvailableDLCs().size() + ")");
                            favoriteBtn.setText(game.isFavorite() ? "❤ Retirer des favoris" : "❤ Ajouter aux favoris");

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
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();

            if (onUpdate != null) onUpdate.run();
        };

        reviewBtn.setOnAction(e -> ReviewDialog.show(game, refreshFromBackend));
        dlcBtn.setOnAction(e -> showDLCsDialog(game, dlcBtn, onUpdate, refreshFromBackend));

        actionsBox.getChildren().addAll(playBtn, reviewBtn, seeReviewsBtn, favoriteBtn, updateNowBtn, dlcBtn);
        
        centerPane.getChildren().addAll(titleLabel, platformLabel, statusLabel, timeLabel, new Separator(), actionsBox);
        
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
    
    private static void showDLCsDialog(Game game, Button dlcBtn, Runnable onUpdate, Runnable refreshCallback) {
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
        // Pre-mark DLCs as installed if backend shows the user purchased them
        try {
            org.example.models.Player current = SessionManager.getInstance().getCurrentPlayer();
            if (current != null) {
                PlatformApiClient api = new PlatformApiClient();
                String purchasesJson = api.getDLCPurchasesForUserJson(current.getId());
                if (purchasesJson != null && !purchasesJson.isEmpty()) {
                    com.fasterxml.jackson.databind.ObjectMapper m = new com.fasterxml.jackson.databind.ObjectMapper();
                    try {
                        java.util.List<java.util.Map<String,Object>> list = m.readValue(purchasesJson, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String,Object>>>() {});
                        java.util.Set<String> purchasedIds = new java.util.HashSet<>();
                        for (java.util.Map<String,Object> entry : list) {
                            Object v = entry.get("dlcId");
                            if (v != null) purchasedIds.add(String.valueOf(v));
                        }
                        if (!purchasedIds.isEmpty()) {
                            for (Game.DLC d : game.getAvailableDLCs()) {
                                try {
                                    if (d.getId() != null && purchasedIds.contains(d.getId())) d.setInstalled(true);
                                } catch (Exception ignore) {}
                            }
                        }
                    } catch (Exception ignore) {
                        // ignore parse errors
                    }
                }
            }
        } catch (Exception ignore) {}
        
        // Afficher tous les DLCs (nom + statut installé + prix / bouton acheter)
        for (Game.DLC dlc : game.getAvailableDLCs()) {
            HBox row = new HBox(10);
            row.setPadding(new javafx.geometry.Insets(10));
            row.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label nameLabel = new Label(dlc.getName());
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label statusLabel = new Label(dlc.isInstalled() ? "✅ Installé" : "⬇ Non installé");
            statusLabel.setStyle("-fx-text-fill: " + (dlc.isInstalled() ? "#4CAF50" : "#FF9800") + ";");

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Label priceLabel = new Label(dlc.getFormattedPrice());
            priceLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 14px;");

            row.getChildren().addAll(nameLabel, statusLabel, spacer, priceLabel);

            if (!dlc.isInstalled()) {
                Button buyBtn = new Button("Acheter");
                buyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                buyBtn.setOnAction(e -> {
                    double price = dlc.getPrice();
                    org.example.models.Player current = SessionManager.getInstance().getCurrentPlayer();
                    if (current == null) {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setContentText("Aucun joueur connecté.");
                        error.showAndWait();
                        return;
                    }
                    if (current.getWallet() < price) {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setContentText("Solde insuffisant !");
                        error.showAndWait();
                        return;
                    }

                    // Perform purchase via platform API, then reload DLCs and purchases
                    new Thread(() -> {
                        PlatformApiClient api = new PlatformApiClient();
                        try {
                            String userId = current.getId();
                            api.purchaseDLC(userId, dlc.getId());

                            javafx.application.Platform.runLater(() -> current.setWallet(current.getWallet() - price));

                            GameDataService.getInstance().reload();

                            String purchasesJson = api.getDLCPurchasesForUserJson(userId);
                            java.util.Set<String> purchasedIds = new java.util.HashSet<>();
                            if (purchasesJson != null && !purchasesJson.isEmpty()) {
                                com.fasterxml.jackson.databind.ObjectMapper m = new com.fasterxml.jackson.databind.ObjectMapper();
                                try {
                                    java.util.List<java.util.Map<String,Object>> list = m.readValue(purchasesJson, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<java.util.Map<String,Object>>>() {});
                                    for (java.util.Map<String,Object> entry : list) {
                                        Object v = entry.get("dlcId");
                                        if (v != null) purchasedIds.add(String.valueOf(v));
                                    }
                                } catch (Exception ex) {
                                    // ignore parse errors
                                }
                            }

                            Game refreshed = GameDataService.getInstance().findGameById(game.getId());
                            if (refreshed != null) {
                                for (Game.DLC d : refreshed.getAvailableDLCs()) {
                                    if (purchasedIds.contains(d.getId())) d.setInstalled(true);
                                }
                            }

                            javafx.application.Platform.runLater(() -> {
                                Alert success = new Alert(Alert.AlertType.INFORMATION);
                                success.setContentText("DLC acheté et enregistré sur la plateforme !");
                                success.showAndWait();
                                dialog.close();
                                dlcBtn.setText("🎁 DLCs (" + game.getAvailableDLCs().size() + ")");
                                if (onUpdate != null) onUpdate.run();
                                if (refreshCallback != null) refreshCallback.run();
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            javafx.application.Platform.runLater(() -> {
                                Alert err = new Alert(Alert.AlertType.ERROR);
                                err.setContentText("Échec de l'achat du DLC: " + ex.getMessage());
                                err.showAndWait();
                            });
                        }
                    }).start();
                });
                row.getChildren().add(buyBtn);
            }

            dlcList.getChildren().add(row);
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

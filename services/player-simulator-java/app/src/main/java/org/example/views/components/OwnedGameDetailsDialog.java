package org.example.views. components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene. image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage. Stage;
import org.example. models.Game;


public class OwnedGameDetailsDialog {
    
    public static void show(Game game, Runnable onUpdate) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(game.getName());
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        // GAUCHE:  Image
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
        
        Label statusLabel = new Label(game.isInstalled() ? "✅ Installé" : "⬇ Pas encore installé");
        statusLabel. setStyle("-fx-text-fill: " + (game.isInstalled() ? "#4CAF50" : "#FF9800") + "; -fx-font-size:  14px;");
        
        Label timeLabel = new Label("Temps de jeu: " + game.getPlayedTime() + " min");
        timeLabel.setStyle("-fx-text-fill: #aaa;");
        
        // Boutons d'action
        VBox actionsBox = new VBox(10);
        
        Button playBtn = new Button(game.isInstalled() ? "▶ JOUER" : "⬇ INSTALLER");
        playBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        playBtn.setMaxWidth(Double.MAX_VALUE);
        playBtn.setOnAction(e -> {
            if (game.isInstalled()) {
                GamePlayDialog.show(game, onUpdate);
            } else {
                GameInstallDialog.show(game, () -> {
                    statusLabel.setText("✅ Installé");
                    statusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 14px;");
                    playBtn.setText("▶ JOUER");
                    if (onUpdate != null) onUpdate.run();
                });
            }
        });
        
        Button reviewBtn = new Button("⭐ Laisser un avis");
        reviewBtn.setMaxWidth(Double.MAX_VALUE);
        reviewBtn.setOnAction(e -> ReviewDialog.show(game));
        
        Button seeReviewsBtn = new Button("👁 Voir les avis (" + game.getReviews().size() + ")");
        seeReviewsBtn.setMaxWidth(Double. MAX_VALUE);
        seeReviewsBtn.setOnAction(e -> ReviewsListDialog. show(game));
        
        Button favoriteBtn = new Button(game.isFavorite() ? "❤ Retirer des favoris" : "🤍 Ajouter aux favoris");
        favoriteBtn.setMaxWidth(Double.MAX_VALUE);
        favoriteBtn.setOnAction(e -> {
            game.setFavorite(!game.isFavorite());
            favoriteBtn.setText(game. isFavorite() ? "❤ Retirer des favoris" : "🤍 Ajouter aux favoris");
            if (onUpdate != null) onUpdate.run();
        });
        
        // Mises à jour
        int pendingUpdates = game.getPendingUpdates().size();
        Button updateBtn = new Button("⬇ Télécharger MAJ (" + pendingUpdates + ")");
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn.setDisable(pendingUpdates == 0);
        updateBtn. setOnAction(e -> showUpdatesDialog(game, updateBtn, onUpdate));
        
        // DLCs
        int pendingDLCs = game.getPendingDLCs().size();
        Button dlcBtn = new Button("🎁 Télécharger DLC (" + pendingDLCs + ")");
        dlcBtn.setMaxWidth(Double.MAX_VALUE);
        dlcBtn.setDisable(pendingDLCs == 0);
        dlcBtn.setOnAction(e -> showDLCsDialog(game, dlcBtn, onUpdate));
        
        actionsBox.getChildren().addAll(playBtn, reviewBtn, seeReviewsBtn, favoriteBtn, updateBtn, dlcBtn);
        
        centerPane.getChildren().addAll(titleLabel, statusLabel, timeLabel, new Separator(), actionsBox);
        
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
            alert. setContentText("Aucune mise à jour disponible.");
            alert.showAndWait();
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType. CONFIRMATION);
        alert.setTitle("Mises à jour disponibles");
        alert.setHeaderText(game.getName());
        alert.setContentText("Installer :\n" + String.join("\n", game.getPendingUpdates()));
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                for (String update : new java.util.ArrayList<>(game. getPendingUpdates())) {
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
        if (game.getPendingDLCs().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Aucun DLC disponible.");
            alert.showAndWait();
            return;
        }
        
        // Créer une dialog personnalisée pour choisir les DLCs
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality. APPLICATION_MODAL);
        dialog.setTitle("DLCs disponibles - " + game.getName());
        
        VBox root = new VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("DLCs disponibles pour " + game.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        VBox dlcList = new VBox(10);
        
        for (Game. DLC dlc : game.getPendingDLCs()) {
            HBox dlcItem = new HBox(10);
            dlcItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            dlcItem.setPadding(new javafx.geometry.Insets(10));
            dlcItem.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
            
            Label nameLabel = new Label(dlc. getName());
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
            
            Label priceLabel = new Label(dlc.getFormattedPrice());
            priceLabel.setStyle("-fx-text-fill:  #4CAF50; -fx-font-weight: bold; -fx-font-size: 14px;");
            
            javafx.scene.layout.Region spacer = new javafx.scene. layout.Region();
            javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            Button buyBtn = new Button("Acheter");
            buyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            buyBtn.setOnAction(e -> {
                if (org.example.services.SessionManager.getInstance().getCurrentPlayer().getWallet() < dlc.getPrice()) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setContentText("Solde insuffisant !");
                    error.showAndWait();
                } else {
                    // Déduire le prix
                    org.example.services.SessionManager. getInstance().getCurrentPlayer()
                        .setWallet(org.example.services.SessionManager.getInstance().getCurrentPlayer().getWallet() - dlc.getPrice());
                    
                    game.installDLC(dlc);
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setContentText("DLC acheté et installé !");
                    success.showAndWait();
                    
                    dialog.close();
                    
                    dlcBtn.setText("🎁 Télécharger DLC (" + game.getPendingDLCs().size() + ")");
                    dlcBtn.setDisable(game.getPendingDLCs().isEmpty());
                    
                    if (onUpdate != null) onUpdate.run();
                }
            });
            
            dlcItem.getChildren().addAll(nameLabel, spacer, priceLabel, buyBtn);
            dlcList.getChildren().add(dlcItem);
        }
        
        Button closeBtn = new Button("Fermer");
        closeBtn.setOnAction(e -> dialog.close());
        
        root.getChildren().addAll(titleLabel, new javafx.scene.control. Separator(), dlcList, closeBtn);
        
        javafx.scene.Scene scene = new javafx.scene. Scene(root, 500, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
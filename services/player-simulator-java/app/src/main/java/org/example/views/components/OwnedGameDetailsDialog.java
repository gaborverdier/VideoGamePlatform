package org.example.views.components;

import javafx.geometry.Insets;
import javafx.geometry. Pos;
import javafx.scene.Scene;
import javafx.scene. control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.models.Game;

public class OwnedGameDetailsDialog {
    
    public static void show(Game game) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(game.getName());
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:  #2b2b2b;");
        
        // GAUCHE:  Image
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(20));
        leftPane.setAlignment(Pos. TOP_CENTER);
        
        ImageView imageView = new ImageView(game. getCoverImage());
        imageView.setFitWidth(300);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        
        leftPane.getChildren().add(imageView);
        
        // CENTRE:  Détails
        VBox centerPane = new VBox(15);
        centerPane.setPadding(new Insets(20));
        
        Label titleLabel = new Label(game.getName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label timeLabel = new Label("Temps de jeu: " + game.getPlayedTime() + " min");
        timeLabel. setStyle("-fx-text-fill: #aaa;");
        
        // Boutons d'action
        VBox actionsBox = new VBox(10);
        
        Button playBtn = new Button("▶ JOUER");
        playBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        playBtn.setMaxWidth(Double.MAX_VALUE);
        playBtn.setOnAction(e -> GamePlayDialog.show(game));
        
        Button reviewBtn = new Button("⭐ Laisser un avis");
        reviewBtn.setMaxWidth(Double.MAX_VALUE);
        reviewBtn.setOnAction(e -> showReviewDialog(game));
        
        Button favoriteBtn = new Button(game.isFavorite() ? "❤ Retirer des favoris" : "🤍 Ajouter aux favoris");
        favoriteBtn.setMaxWidth(Double.MAX_VALUE);
        favoriteBtn.setOnAction(e -> {
            game.setFavorite(!game. isFavorite());
            favoriteBtn.setText(game.isFavorite() ? "❤ Retirer des favoris" : "🤍 Ajouter aux favoris");
        });
        
        Button updateBtn = new Button("⬇ Télécharger MAJ (" + game.getAvailableUpdates().size() + ")");
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn.setOnAction(e -> showUpdatesDialog(game));
        
        Button dlcBtn = new Button("🎁 Télécharger DLC (" + game.getAvailableDLCs().size() + ")");
        dlcBtn.setMaxWidth(Double.MAX_VALUE);
        dlcBtn.setOnAction(e -> showDLCsDialog(game));
        
        actionsBox.getChildren().addAll(playBtn, reviewBtn, favoriteBtn, updateBtn, dlcBtn);
        
        centerPane.getChildren().addAll(titleLabel, timeLabel, new Separator(), actionsBox);
        
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
        
        Scene scene = new Scene(root, 600, 450);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private static void showReviewDialog(Game game) {
        TextInputDialog review = new TextInputDialog();
        review.setTitle("Laisser un avis");
        review.setHeaderText(game.getName());
        review.setContentText("Votre avis:");
        review.showAndWait().ifPresent(text -> {
            Alert confirm = new Alert(Alert.AlertType.INFORMATION);
            confirm. setContentText("Avis envoyé !");
            confirm.showAndWait();
        });
    }
    
    private static void showUpdatesDialog(Game game) {
        if (game.getAvailableUpdates().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Aucune mise à jour disponible.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Mises à jour disponibles");
            alert.setHeaderText(game. getName());
            alert.setContentText(String.join("\n", game.getAvailableUpdates()));
            alert.showAndWait();
        }
    }
    
    private static void showDLCsDialog(Game game) {
        if (game.getAvailableDLCs().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType. INFORMATION);
            alert.setContentText("Aucun DLC disponible.");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("DLCs disponibles");
            alert. setHeaderText(game.getName());
            alert.setContentText(String.join("\n", game. getAvailableDLCs()));
            alert.showAndWait();
        }
    }
}
package org.example.views.components.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.models.Game;
import org.example.models.Review;
import org.example.services.SessionManager;

public class ReviewDialog {
    
    public static void show(Game game) {
        if (game.getPlayedTime() < 30) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Temps de jeu insuffisant");
            alert.setContentText("Vous devez jouer au moins 30 minutes avant de pouvoir laisser un avis.");
            alert.showAndWait();
            return;
        }
        
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Évaluer " + game.getName());
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("Évaluer " + game.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Sélection étoiles
        Label ratingLabel = new Label("Note:");
        ratingLabel.setStyle("-fx-text-fill: white;");
        
        HBox starsBox = new HBox(5);
        ToggleGroup starsGroup = new ToggleGroup();
        for (int i = 1; i <= 5; i++) {
            RadioButton star = new RadioButton(i + " ★");
            star.setToggleGroup(starsGroup);
            star.setUserData(i);
            star.setStyle("-fx-text-fill: #FFD700;");
            starsBox.getChildren().add(star);
        }
        
        // Commentaire
        Label commentLabel = new Label("Commentaire:");
        commentLabel.setStyle("-fx-text-fill: white;");
        
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Partagez votre expérience...");
        commentArea.setPrefRowCount(4);
        
        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setOnAction(e -> dialog.close());
        
        Button submitBtn = new Button("Publier");
        submitBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        submitBtn.setOnAction(e -> {
            if (starsGroup.getSelectedToggle() == null || commentArea.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Veuillez remplir tous les champs !");
                alert.showAndWait();
            } else {
                int rating = (int) starsGroup.getSelectedToggle().getUserData();
                String comment = commentArea.getText();
                
                Review review = new Review(
                    game.getId(),
                    SessionManager.getInstance().getCurrentPlayer().getId(),
                    SessionManager.getInstance().getCurrentPlayer().getUsername(),
                    rating,
                    comment,
                    game.getPlayedTime()
                );
                
                game.addReview(review);
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setContentText("Avis publié !");
                success.showAndWait();
                
                dialog.close();
            }
        });
        
        buttonBox.getChildren().addAll(cancelBtn, submitBtn);
        
        root.getChildren().addAll(titleLabel, ratingLabel, starsBox, commentLabel, commentArea, buttonBox);
        
        Scene scene = new Scene(root, 500, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    public static void showForDLC(Game.DLC dlc) {
        if (dlc.getPlayedTime() < 15) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Temps de jeu insuffisant");
            alert.setContentText("Vous devez jouer au moins 15 minutes au DLC avant de pouvoir laisser un avis.");
            alert.showAndWait();
            return;
        }
        
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Évaluer " + dlc.getName());
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("Évaluer " + dlc.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Sélection étoiles
        Label ratingLabel = new Label("Note:");
        ratingLabel.setStyle("-fx-text-fill: white;");
        
        HBox starsBox = new HBox(5);
        ToggleGroup starsGroup = new ToggleGroup();
        for (int i = 1; i <= 5; i++) {
            RadioButton star = new RadioButton(i + " ★");
            star.setToggleGroup(starsGroup);
            star.setUserData(i);
            star.setStyle("-fx-text-fill: #FFD700;");
            starsBox.getChildren().add(star);
        }
        
        // Commentaire
        Label commentLabel = new Label("Commentaire:");
        commentLabel.setStyle("-fx-text-fill: white;");
        
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Partagez votre expérience...");
        commentArea.setPrefRowCount(4);
        
        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setOnAction(e -> dialog.close());
        
        Button submitBtn = new Button("Publier");
        submitBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        submitBtn.setOnAction(e -> {
            if (starsGroup.getSelectedToggle() == null || commentArea.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Veuillez remplir tous les champs !");
                alert.showAndWait();
            } else {
                int rating = (int) starsGroup.getSelectedToggle().getUserData();
                String comment = commentArea.getText();
                
                Review review = new Review(
                    dlc.getName(),
                    SessionManager.getInstance().getCurrentPlayer().getId(),
                    SessionManager.getInstance().getCurrentPlayer().getUsername(),
                    rating,
                    comment,
                    dlc.getPlayedTime()
                );
                
                dlc.addReview(review);
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setContentText("Avis publié !");
                success.showAndWait();
                
                dialog.close();
            }
        });
        
        buttonBox.getChildren().addAll(cancelBtn, submitBtn);
        
        root.getChildren().addAll(titleLabel, ratingLabel, starsBox, commentLabel, commentArea, buttonBox);
        
        Scene scene = new Scene(root, 500, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

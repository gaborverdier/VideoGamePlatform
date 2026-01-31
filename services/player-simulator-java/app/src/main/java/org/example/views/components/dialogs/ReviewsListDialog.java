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

public class ReviewsListDialog {
    
    public static void show(Game game) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Avis - " + game.getName());
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("Avis des joueurs - " + game.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        
        VBox reviewsList = new VBox(10);
        reviewsList.setPadding(new Insets(10));
        
        if (game.getReviews().isEmpty()) {
            Label noReviews = new Label("Aucun avis pour le moment.");
            noReviews.setStyle("-fx-text-fill: #aaa;");
            reviewsList.getChildren().add(noReviews);
        } else {
            for (Review review : game.getReviews()) {
                reviewsList.getChildren().add(createReviewCard(review));
            }
        }
        
        scrollPane.setContent(reviewsList);
        
        Button closeBtn = new Button("Fermer");
        closeBtn.setOnAction(e -> dialog.close());
        
        root.getChildren().addAll(titleLabel, new Separator(), scrollPane, closeBtn);
        
        Scene scene = new Scene(root, 600, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private static VBox createReviewCard(Review review) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label authorLabel = new Label(review.getAuthorName());
        authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        
        Label starsLabel = new Label(review.getStars());
        starsLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 14px;");
        
        header.getChildren().addAll(authorLabel, starsLabel);
        
        Label commentLabel = new Label(review.getComment());
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-text-fill: white;");
        
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        
        String currentPlayerId = SessionManager.getInstance().getCurrentPlayer().getId();
        
        Button helpfulBtn = new Button("👍 Utile (" + review.getHelpfulCount() + ")");
        helpfulBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4CAF50;");
        helpfulBtn.setOnAction(e -> {
            if (review.markHelpful(currentPlayerId)) {
                helpfulBtn.setText("👍 Utile (" + review.getHelpfulCount() + ")");
            }
        });
        
        Button notHelpfulBtn = new Button("👎 Pas utile (" + review.getNotHelpfulCount() + ")");
        notHelpfulBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336;");
        notHelpfulBtn.setOnAction(e -> {
            if (review.markNotHelpful(currentPlayerId)) {
                notHelpfulBtn.setText("👎 Pas utile (" + review.getNotHelpfulCount() + ")");
            }
        });
        
        footer.getChildren().addAll(helpfulBtn, notHelpfulBtn);
        
        card.getChildren().addAll(header, commentLabel, footer);
        
        return card;
    }
    
    public static void showForDLC(Game.DLC dlc) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Avis - " + dlc.getName());
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("Avis des joueurs - " + dlc.getName());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        
        VBox reviewsList = new VBox(10);
        reviewsList.setPadding(new Insets(10));
        
        if (dlc.getReviews().isEmpty()) {
            Label noReviews = new Label("Aucun avis pour le moment.");
            noReviews.setStyle("-fx-text-fill: #aaa;");
            reviewsList.getChildren().add(noReviews);
        } else {
            for (Review review : dlc.getReviews()) {
                reviewsList.getChildren().add(createReviewCard(review));
            }
        }
        
        scrollPane.setContent(reviewsList);
        
        Button closeBtn = new Button("Fermer");
        closeBtn.setOnAction(e -> dialog.close());
        
        root.getChildren().addAll(titleLabel, new Separator(), scrollPane, closeBtn);
        
        Scene scene = new Scene(root, 600, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

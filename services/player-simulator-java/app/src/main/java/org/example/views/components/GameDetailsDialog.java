package org.example.views. components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene. control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx. stage. Modality;
import javafx.stage.Stage;
import org.example.models.Game;
import org.example.services.SessionManager;

public class GameDetailsDialog {
    
    private Game game;
    private boolean purchaseConfirmed = false;
    
    public GameDetailsDialog(Game game) {
        this.game = game;
    }
    
    public boolean show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(game.getName());
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:  #2b2b2b;");
        
        // GAUCHE : Image
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(20));
        leftPane.setAlignment(Pos. TOP_CENTER);
        
        ImageView imageView = new ImageView(game. getCoverImage());
        imageView.setFitWidth(300);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        
        leftPane.getChildren().add(imageView);
        
        // CENTRE : Détails
        VBox centerPane = new VBox(15);
        centerPane.setPadding(new Insets(20));
        
        Label titleLabel = new Label(game.getName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label genreLabel = new Label("Genre:  " + game.getGenre());
        genreLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaa;");
        
        Label publisherLabel = new Label("Éditeur: " + game. getPublisherName());
        publisherLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaa;");
        
        // Note moyenne
        HBox ratingBox = new HBox(5);
        Label ratingLabel = new Label("Note:  ");
        ratingLabel.setStyle("-fx-text-fill: white;");
        Label starsLabel = new Label(getStars(game.getAverageRating()) + " (" + String.format("%.1f", game.getAverageRating()) + "/5)");
        starsLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16px;");
        Label reviewCountLabel = new Label(" - " + game.getReviews().size() + " avis");
        reviewCountLabel.setStyle("-fx-text-fill: #aaa;");
        ratingBox.getChildren().addAll(ratingLabel, starsLabel, reviewCountLabel);
        
        Button seeReviewsBtn = new Button("Voir les avis");
        seeReviewsBtn. setOnAction(e -> ReviewsListDialog.show(game));
        
        // Description
        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        
        TextArea descArea = new TextArea(game.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(5);
        descArea.setStyle("-fx-control-inner-background: #3c3c3c; -fx-text-fill: white;");
        
        // Prix
        Label priceLabel = new Label(game.getFormattedPrice());
        priceLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        centerPane. getChildren().addAll(
            titleLabel, genreLabel, publisherLabel, ratingBox, seeReviewsBtn,
            new Separator(), descLabel, descArea,
            new Separator(), priceLabel
        );
        
        // BAS : Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(20));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button wishlistBtn = new Button(game.isWishlisted() ? "❤ Retirer de la liste" : "🤍 Ajouter à la liste de souhaits");
        wishlistBtn.setOnAction(e -> {
            game.setWishlisted(!game.isWishlisted());
            wishlistBtn. setText(game.isWishlisted() ? "❤ Retirer de la liste" : "🤍 Ajouter à la liste de souhaits");
        });
        
        Button cancelBtn = new Button("Fermer");
        cancelBtn.setStyle("-fx-background-color:  #555; -fx-text-fill: white; -fx-font-size: 14px;");
        cancelBtn.setOnAction(e -> dialog.close());
        
        Button buyBtn = new Button(game.isOwned() ? "Déjà possédé" : "Acheter");
        buyBtn.setDisable(game.isOwned());
        buyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill:  white; -fx-font-size: 14px; -fx-font-weight: bold;");
        buyBtn.setOnAction(e -> {
            if (SessionManager.getInstance().getCurrentPlayer().purchaseGame(game)) {
                purchaseConfirmed = true;
                
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success. setTitle("Achat réussi");
                success.setContentText("Le jeu a été ajouté à vos jeux !\nN'oubliez pas de l'installer avant de jouer.");
                success.showAndWait();
                
                dialog.close();
            } else {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error. setContentText("Solde insuffisant !");
                error.showAndWait();
            }
        });
        
        buttonBox.getChildren().addAll(wishlistBtn, cancelBtn, buyBtn);
        
        // Assemblage
        root.setLeft(leftPane);
        root.setCenter(centerPane);
        root.setBottom(buttonBox);
        
        Scene scene = new Scene(root, 750, 550);
        dialog.setScene(scene);
        dialog.showAndWait();
        
        return purchaseConfirmed;
    }
    
    private String getStars(double rating) {
        int fullStars = (int) rating;
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        for (int i = fullStars; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }
}
package org.example.views.components.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.models.Game;
import org.example.models.Platform;
import org.example.models.Player;
import org.example.services.GameDataService;
import org.example.services.SessionManager;

public class GameDetailsDialog {
    
    private Game game;
    private boolean purchaseConfirmed = false;
    private Runnable onWishlistChanged;
    
    public GameDetailsDialog(Game game) {
        this(game, null);
    }
    
    public GameDetailsDialog(Game game, Runnable onWishlistChanged) {
        this.game = game;
        this.onWishlistChanged = onWishlistChanged;
    }
    
    public boolean show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(game.getName());
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        // GAUCHE : Image
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(20));
        leftPane.setAlignment(Pos.TOP_CENTER);
        
        ImageView imageView = new ImageView(game.getCoverImage());
        imageView.setFitWidth(300);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(true);
        
        leftPane.getChildren().add(imageView);
        
        // CENTRE : Détails
        VBox centerPane = new VBox(15);
        centerPane.setPadding(new Insets(20));
        
        Label titleLabel = new Label(game.getName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label genreLabel = new Label("Genre: " + game.getGenre());
        genreLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaa;");
        
        Label publisherLabel = new Label("Éditeur: " + game.getPublisherName());
        publisherLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #66c2ff; -fx-underline: true; -fx-cursor: hand;");
        publisherLabel.setOnMouseClicked(e -> showPublisherPage(game.getPublisherName()));
        
        // Note moyenne
        HBox ratingBox = new HBox(5);
        Label ratingLabel = new Label("Note: ");
        ratingLabel.setStyle("-fx-text-fill: white;");
        Label starsLabel = new Label(getStars(game.getAverageRating()) + " (" + String.format("%.1f", game.getAverageRating()) + "/5)");
        starsLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 16px;");
        Label reviewCountLabel = new Label(" - " + game.getReviews().size() + " avis");
        reviewCountLabel.setStyle("-fx-text-fill: #aaa;");
        ratingBox.getChildren().addAll(ratingLabel, starsLabel, reviewCountLabel);
        
        Button seeReviewsBtn = new Button("Voir les avis");
        seeReviewsBtn.setOnAction(e -> ReviewsListDialog.show(game));

        // Plateformes disponibles
        Label platformTitle = new Label("Support :");
        platformTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");

        ToggleGroup platformGroup = new ToggleGroup();
        VBox platformBox = new VBox(8);
        platformBox.setPadding(new Insets(5, 0, 0, 0));

        Platform defaultPlatform = null;
        for (Platform platform : game.getSupportedPlatforms()) {
            RadioButton option = new RadioButton(platform.getLabel());
            option.setToggleGroup(platformGroup);
            option.setUserData(platform);
            option.setStyle("-fx-text-fill: white;");
            platformBox.getChildren().add(option);
            if (defaultPlatform == null && !game.isOwnedOnPlatform(platform)) {
                defaultPlatform = platform;
                option.setSelected(true);
            }
            if (defaultPlatform == null) {
                defaultPlatform = platform;
                option.setSelected(true);
            }
        }
        
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
        
        centerPane.getChildren().addAll(
            titleLabel, genreLabel, publisherLabel, ratingBox, seeReviewsBtn,
            new Separator(), platformTitle, platformBox,
            new Separator(), descLabel, descArea,
            new Separator(), priceLabel
        );
        
        // BAS : Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(20));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button wishlistBtn = new Button(game.isWishlisted() ? "❤ Retirer de la liste" : "🤍 Ajouter à la liste de souhaits");
        wishlistBtn.setDisable(game.isOwned());
        if (game.isOwned()) {
            wishlistBtn.setText("Déjà possédé");
        }
        wishlistBtn.setOnAction(e -> {
            game.setWishlisted(!game.isWishlisted());
            wishlistBtn.setText(game.isWishlisted() ? "❤ Retirer de la liste" : "🤍 Ajouter à la liste de souhaits");
            if (onWishlistChanged != null) {
                onWishlistChanged.run();
            }
        });
        
        Button cancelBtn = new Button("Fermer");
        cancelBtn.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 14px;");
        cancelBtn.setOnAction(e -> dialog.close());
        
        boolean ownsAllPlatforms = game.ownsAllSupportedPlatforms();
        Button buyBtn = new Button(ownsAllPlatforms ? "Déjà possédé sur tous les supports" : "Acheter");
        buyBtn.setDisable(ownsAllPlatforms);
        buyBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        buyBtn.setOnAction(e -> {
            Toggle selected = platformGroup.getSelectedToggle();
            if (selected == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Choisissez un support avant d'acheter.");
                alert.showAndWait();
                return;
            }

            Platform chosenPlatform = (Platform) selected.getUserData();
            Player.PurchaseResult result = SessionManager.getInstance()
                    .getCurrentPlayer()
                    .purchaseGame(game, chosenPlatform);

            switch (result) {
                case SUCCESS -> {
                    // Persist purchase to backend
                    org.example.models.Player current = org.example.services.SessionManager.getInstance().getCurrentPlayer();
                    String userId = current != null ? current.getId() : null;
                    if (userId != null) {
                        try {
                            org.example.services.GameDataService.getInstance().purchaseGameForUser(userId, game.getId());
                            System.out.println("Persisted purchase for user " + userId + " game " + game.getId());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Alert err = new Alert(Alert.AlertType.ERROR);
                            err.setTitle("Erreur backend");
                            err.setContentText("Impossible d'enregistrer l'achat sur le serveur : " + ex.getMessage());
                            err.showAndWait();
                        }
                    } else {
                        System.out.println("No logged-in user: skipping backend purchase persist.");
                    }

                    purchaseConfirmed = true;
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Achat réussi");
                    success.setContentText("Le jeu a été ajouté sur " + chosenPlatform.getLabel() + " !\nN'oubliez pas de l'installer avant de jouer.");
                    success.showAndWait();
                    boolean nowOwnsAll = game.ownsAllSupportedPlatforms();
                    if (nowOwnsAll) {
                        buyBtn.setText("Déjà possédé sur tous les supports");
                        buyBtn.setDisable(true);
                        wishlistBtn.setText("Déjà possédé");
                        wishlistBtn.setDisable(true);
                    }
                    dialog.close();
                }
                case INSUFFICIENT_FUNDS -> {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setContentText("Solde insuffisant !");
                    error.showAndWait();
                }
                case ALREADY_OWNED -> {
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setContentText("Tu possèdes déjà ce jeu sur " + chosenPlatform.getLabel() + ".");
                    info.showAndWait();
                }
                default -> {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setContentText("Support non disponible pour ce jeu.");
                    error.showAndWait();
                }
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

    private void showPublisherPage(String publisherName) {
        java.util.List<Game> games = GameDataService.getInstance().getAllGames().stream()
                .filter(g -> publisherName.equals(g.getPublisherName()))
                .sorted(java.util.Comparator.comparing(Game::getName))
                .toList();

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Éditeur : " + publisherName);

        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label(publisherName + " - " + games.size() + " jeu(x)");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox list = new VBox(8);
        if (games.isEmpty()) {
            list.getChildren().add(new Label("Aucun jeu pour cet éditeur."));
        } else {
            for (Game g : games) {
                HBox row = new HBox(8);
                row.setAlignment(Pos.CENTER_LEFT);
                Label name = new Label(g.getName());
                name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                Label meta = new Label(g.getGenre() + " • " + g.getSupportedPlatformsLabel());
                meta.setStyle("-fx-text-fill: #aaa;");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Button openBtn = new Button("Ouvrir");
                openBtn.setOnAction(e -> new GameDetailsDialog(g).show());
                row.getChildren().addAll(name, meta, spacer, openBtn);
                list.getChildren().add(row);
            }
        }

        Button close = new Button("Fermer");
        close.setOnAction(e -> dialog.close());
        HBox footer = new HBox(close);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(title, new Separator(), list, new Separator(), footer);

        Scene scene = new Scene(root, 520, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

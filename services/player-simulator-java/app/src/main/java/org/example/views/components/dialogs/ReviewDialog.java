package org.example.views.components.dialogs;

import org.example.models.Game;
import org.example.models.Review;
import org.example.services.SessionManager;
import org.example.services.PlatformApiClient;
import org.example.util.ApiClient;
import org.example.util.AvroJacksonConfig;

import com.gaming.events.GameReviewed;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ReviewDialog {

    public static void show(Game game, Runnable onSuccess) {
        // Check playtime (local and backend) asynchronously, then open dialog if allowed
        new Thread(() -> {
            try {
                long localMin = game.getPlayedTime();
                long backendMin = 0L;
                try {
                    String userId = SessionManager.getInstance().getCurrentPlayer() != null ? SessionManager.getInstance().getCurrentPlayer().getId() : null;
                    if (userId != null) {
                        PlatformApiClient api = new PlatformApiClient();
                        long totalMs = api.getTotalPlayedForUser(game.getId(), userId);
                        backendMin = totalMs / 60_000L;
                    }
                } catch (Exception ex) {
                    // best-effort: ignore backend failures
                }

                long effectiveMin = Math.max(localMin, backendMin);
                if (effectiveMin < 30) {
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Temps de jeu insuffisant");
                        alert.setContentText("Vous devez jouer au moins 30 minutes avant de pouvoir laisser un avis.");
                        alert.showAndWait();
                    });
                    return;
                }

                // open UI on FX thread
                javafx.application.Platform.runLater(() -> {
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
                        game.getPlayedTime());

                game.addReview(review);

                postReview(review);

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setContentText("Avis publié !");
                success.showAndWait();

                // notify caller so UI can refresh from backend
                try {
                    if (onSuccess != null) onSuccess.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                dialog.close();
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, submitBtn);

        root.getChildren().addAll(titleLabel, ratingLabel, starsBox, commentLabel, commentArea, buttonBox);

                    Scene scene = new Scene(root, 500, 400);
                    dialog.setScene(scene);
                    dialog.showAndWait();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void postReview(Review review) {
        GameReviewed toSend = new GameReviewed();
        toSend.setGameId(review.getGameId());
        toSend.setUserId(review.getAuthorId());
        toSend.setUsername(review.getAuthorName());
        toSend.setRating(review.getRating());
        toSend.setReviewText(review.getComment());

        try {
            String json = AvroJacksonConfig.avroObjectMapper().writeValueAsString(toSend);
            ApiClient.postJson("/api/reviews", json);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void showForDLC(Game.DLC dlc, Runnable onSuccess) {
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
                        dlc.getPlayedTime());

                dlc.addReview(review);

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setContentText("Avis publié !");
                success.showAndWait();

                try {
                    if (onSuccess != null) onSuccess.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

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

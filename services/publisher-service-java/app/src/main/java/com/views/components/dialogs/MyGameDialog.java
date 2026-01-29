package com.views.components.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.model.Game;
import com.model.Crash;
import com.model.Patch;
import com.model.DLC;
import com.model.Review;
import com.views.components.tabs.NotificationsTab;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MyGameDialog {

    public static void show(Game game, NotificationsTab notificationsTab) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Détails du jeu - " + (game.getTitle() == null ? "(sans titre)" : game.getTitle()));

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");

        Label header = new Label(game.getTitle());
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        HBox headerBox = new HBox(header);
        headerBox.setPadding(new Insets(12));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Reviews tab
        VBox reviewsBox = new VBox(8);
        reviewsBox.setPadding(new Insets(12));
        reviewsBox.setStyle("-fx-background-color: #2b2b2b;");
        List<Review> gameReviews = notificationsTab != null ? notificationsTab.getReviews().stream()
            .filter(r -> game.getTitle() != null && r.getGameName() != null && r.getGameName().equals(game.getTitle()))
            .sorted((r1,r2)->r2.getTimestamp().compareTo(r1.getTimestamp()))
            .collect(Collectors.toList()) : List.of();

        if (gameReviews.isEmpty()) {
            reviewsBox.getChildren().add(new Label("Aucun avis pour ce jeu."));
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Review r : gameReviews) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(8));
                card.setStyle("-fx-background-color:#3c3c3c; -fx-background-radius:4;");
                Label who = new Label(r.getPlayerName() + " — " + r.getRatingStars());
                who.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                String pt = (r.getPlaytimeMinutes() != null) ? (" | " + r.getPlaytimeMinutes() + " min") : "";
                Label meta = new Label(r.getTimestamp().format(fmt) + pt);
                meta.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
                Label comment = new Label(r.getComment());
                comment.setStyle("-fx-text-fill: #ddd; -fx-wrap-text: true;");
                comment.setWrapText(true);
                card.getChildren().addAll(who, meta, comment);
                reviewsBox.getChildren().add(card);
            }
        }

        Tab reviewsTab = new Tab("Avis", new ScrollPane(reviewsBox));

        // Crash reports tab
        VBox crashesBox = new VBox(8);
        crashesBox.setPadding(new Insets(12));
        List<Crash> gameCrashes = notificationsTab != null ? notificationsTab.getCrashReports().stream()
            .filter(c -> c.getGame() != null && c.getGame().getTitle() != null && c.getGame().getTitle().equals(game.getTitle()))
            .collect(Collectors.toList()) : List.of();

        if (gameCrashes.isEmpty()) {
            crashesBox.getChildren().add(new Label("Aucun rapport de crash pour ce jeu."));
        } else {
            for (Crash c : gameCrashes) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(8));
                card.setStyle("-fx-background-color:#4a2a2a; -fx-background-radius:4; -fx-border-color:#FF6B6B; -fx-border-width:1;");
                Label date = new Label(c.getCrashTimeStamp().toString());
                date.setStyle("-fx-text-fill:#999; -fx-font-size:11px;");
                Label desc = new Label(c.getErrorMessage());
                desc.setStyle("-fx-text-fill:#ffb3b3; -fx-wrap-text: true;");
                desc.setWrapText(true);
                card.getChildren().addAll(date, desc);
                crashesBox.getChildren().add(card);
            }
        }
        Tab crashesTab = new Tab("Crash Reports", new ScrollPane(crashesBox));

        // DLCs & Patches tab
        VBox contentBox = new VBox(12);
        contentBox.setPadding(new Insets(12));

        // DLCs
        Label dlcTitle = new Label("DLCs");
        dlcTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        contentBox.getChildren().add(dlcTitle);
        List<DLC> dlcs = game.getDlcs() == null ? List.of() : game.getDlcs();
        if (dlcs.isEmpty()) {
            contentBox.getChildren().add(new Label("Aucun DLC publié."));
        } else {
            for (DLC d : dlcs) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(8));
                card.setStyle("-fx-background-color:#3c3c3c; -fx-background-radius:4;");
                Label name = new Label(d.getName() + " — " + (d.getReleaseTimeStamp() == null ? "" : d.getReleaseTimeStamp()));
                name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                Label desc = new Label(d.getDescription() == null ? "" : d.getDescription());
                desc.setStyle("-fx-text-fill:#ddd; -fx-wrap-text:true;");
                desc.setWrapText(true);
                contentBox.getChildren().addAll(card, name, desc);
            }
        }

        // Patches
        Label patchTitle = new Label("Patches / Mises à jour");
        patchTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        contentBox.getChildren().add(patchTitle);
        List<Patch> patches = game.getPatches() == null ? List.of() : game.getPatches();
        if (patches.isEmpty()) {
            contentBox.getChildren().add(new Label("Aucune mise à jour publiée."));
        } else {
            for (Patch p : patches) {
                VBox card = new VBox(6);
                card.setPadding(new Insets(8));
                card.setStyle("-fx-background-color:#3c3c3c; -fx-background-radius:4;");
                Label ver = new Label("v" + (p.getVersion() == null ? "" : p.getVersion()) + " — " + (p.getReleaseTimeStamp() == null ? "" : p.getReleaseTimeStamp()));
                ver.setStyle("-fx-text-fill:white; -fx-font-weight: bold;");
                Label desc = new Label(p.getDescription() == null ? "" : String.join("\n", p.getDescription().split("\n")));
                desc.setStyle("-fx-text-fill:#ddd; -fx-wrap-text:true;");
                desc.setWrapText(true);
                contentBox.getChildren().addAll(card, ver, desc);
            }
        }

        Tab contentTab = new Tab("DLCs & Patches", new ScrollPane(contentBox));

        tabPane.getTabs().addAll(reviewsTab, crashesTab, contentTab);

        root.setTop(headerBox);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 700, 600);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

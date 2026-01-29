package com.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.views.components.tabs.MyGamesTab;
import com.views.components.tabs.NotificationsTab;
import com.views.components.dialogs.PublisherLoginDialog;

public class PublisherDashboard {

    private Stage primaryStage;
    private String publisherName;
    private String publisherType;
    private MyGamesTab myGamesTab;
    private NotificationsTab notificationsTab;

    public PublisherDashboard(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void show() {
        // Login dialog
        String[] loginData = PublisherLoginDialog.show();
        
        if (loginData[0] == null) {
            primaryStage.close();
            return;
        }

        this.publisherName = loginData[0];
        this.publisherType = loginData[2];

        primaryStage.setTitle("Tableau de Bord Éditeur - " + publisherName);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(700);

        // Layout principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #2b2b2b;");

        // Header
        VBox header = createHeader();
        root.setTop(header);

        // TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #2b2b2b;");

        // Tab 1: Mes jeux and Tab 2: Notifications
        notificationsTab = new NotificationsTab();
        myGamesTab = new MyGamesTab(notificationsTab, () -> {
            if (notificationsTab != null) notificationsTab.setVisible(true);
        });
        Tab gamesTab = new Tab("Mes jeux", myGamesTab);
        gamesTab.setStyle("-fx-font-size: 12px;");

        Tab notificationsTabComponent = new Tab("Notifications", notificationsTab);
        notificationsTabComponent.setStyle("-fx-font-size: 12px;");

        tabPane.getTabs().addAll(gamesTab, notificationsTabComponent);

        root.setCenter(tabPane);

        // Footer
        HBox footer = createFooter();
        root.setBottom(footer);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: #1a1a1a;");

        HBox titleBox = new HBox(20);
        titleBox.setAlignment(Pos.CENTER_LEFT);


        VBox titleVBox = new VBox(5);
        Label titleLabel = new Label("Plateforme Éditeurs");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("Bienvenue, " + publisherName + " (" + (publisherType.equals("COMPANY") ? "Entreprise" : "Indépendant") + ")");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaa;");

        titleVBox.getChildren().addAll(titleLabel, subtitleLabel);
        titleBox.getChildren().add(titleVBox);

        header.getChildren().add(titleBox);

        return header;
    }

    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setPadding(new Insets(10));
        footer.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #3c3c3c; -fx-border-width: 1 0 0 0;");
        footer.setAlignment(Pos.CENTER_RIGHT);

        Label statsLabel = new Label("Jeux publiés: " + myGamesTab.getPublishedGames().size());
        statsLabel.setStyle("-fx-text-fill: white;");

        Button logoutButton = new Button("Déconnexion");
        logoutButton.setStyle("-fx-padding: 8px 15px; -fx-font-size: 11px;");
        logoutButton.setOnAction(e -> {
            primaryStage.close();
        });

        footer.getChildren().addAll(statsLabel, new Separator(), logoutButton);

        return footer;
    }
}

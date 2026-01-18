package org.example.views;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.example.models.Game;
import org.example.views.components.LibraryTab;
import org.example.views.components.MyGamesTab;

public class PlayerDashboard extends Application {

    private MyGamesTab myGamesTab;
    private LibraryTab libraryTab;

    @Override
    public void start(Stage stage) {
        TabPane mainLayout = new TabPane();
        mainLayout.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Créer les onglets
        myGamesTab = new MyGamesTab();
        libraryTab = new LibraryTab(this:: onGamePurchased);

        Tab libraryTabUI = new Tab("Bibliothèque", libraryTab);
        Tab myGamesTabUI = new Tab("Mes Jeux", myGamesTab);

        mainLayout.getTabs().addAll(libraryTabUI, myGamesTabUI);

        Scene scene = new Scene(mainLayout, 900, 650);
        stage.setTitle("Player Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    // Callback quand un jeu est acheté
    private void onGamePurchased(Game game) {
        myGamesTab.addOwnedGame(game);
    }

    public static void main(String[] args) {
        launch();
    }
}
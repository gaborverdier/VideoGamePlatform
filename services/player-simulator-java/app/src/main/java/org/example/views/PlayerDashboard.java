package org. example.views;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx. scene.control. Tab;
import javafx.scene. control.TabPane;
import javafx.stage.Stage;
import org.example.models.Game;
import org.example.services.GameDataService;
import org.example.views.components.*;

public class PlayerDashboard extends Application {

    private MyGamesTab myGamesTab;
    private LibraryTab libraryTab;
    private WishlistTab wishlistTab;
    private NotificationsTab notificationsTab;

    @Override
    public void start(Stage stage) {
        // Afficher la fenêtre de connexion
        if (! LoginDialog.show()) {
            return; // L'utilisateur a quitté
        }
        
        TabPane mainLayout = new TabPane();
        mainLayout.setTabClosingPolicy(TabPane.TabClosingPolicy. UNAVAILABLE);

        // Créer les onglets
        libraryTab = new LibraryTab(this:: onGamePurchased);
        myGamesTab = new MyGamesTab(this::refreshAll);
        wishlistTab = new WishlistTab(this::onGamePurchased);
        notificationsTab = new NotificationsTab();

        Tab libraryTabUI = new Tab("📚 Bibliothèque", libraryTab);
        Tab myGamesTabUI = new Tab("🎮 Mes Jeux", myGamesTab);
        Tab wishlistTabUI = new Tab("❤ Liste de souhaits", wishlistTab);
        Tab notificationsTabUI = new Tab("🔔 Notifications", notificationsTab);

        mainLayout. getTabs().addAll(libraryTabUI, myGamesTabUI, wishlistTabUI, notificationsTabUI);

        Scene scene = new Scene(mainLayout, 950, 700);
        stage.setTitle("Player Dashboard");
        stage.setScene(scene);
        stage.show();
        
        // Initialiser les notifications
        refreshAll();
    }

    private void onGamePurchased(Game game) {
        myGamesTab.addOwnedGame(game);
        refreshAll();
    }
    
    private void refreshAll() {
        myGamesTab. refresh();
        wishlistTab.refresh();
        notificationsTab.setGames(GameDataService.getInstance().getAllGames());
    }

    public static void main(String[] args) {
        launch();
    }
}
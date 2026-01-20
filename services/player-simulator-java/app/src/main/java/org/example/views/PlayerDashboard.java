package org.example.views;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.example.models.Game;
import org.example.services.GameDataService;
import org.example.services.SessionManager;
import org.example.views.components.*;

public class PlayerDashboard extends Application {

    private MyGamesTab myGamesTab;
    private LibraryTab libraryTab;
    private WishlistTab wishlistTab;
    private NotificationsTab notificationsTab;
    private PublishersTab publishersTab;
    private FriendsTab friendsTab;
    private javafx.scene.control.Label walletLabel;

    @Override
    public void start(Stage stage) {
        // Afficher la fenêtre de connexion
        if (! LoginDialog.show()) {
            return; // L'utilisateur a quitté
        }
        
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy. UNAVAILABLE);

        // Créer les onglets
        libraryTab = new LibraryTab(this::onGamePurchased);
        libraryTab.setOnRefreshAll(this::refreshAll);
        myGamesTab = new MyGamesTab(this::refreshAll);
        wishlistTab = new WishlistTab(this::onGamePurchased);
        wishlistTab.setLibraryTab(libraryTab);
        publishersTab = new PublishersTab(this::onGamePurchased);
        friendsTab = new FriendsTab();
        notificationsTab = new NotificationsTab();

        Tab libraryTabUI = new Tab("📚 Bibliothèque", libraryTab);
        Tab myGamesTabUI = new Tab("🎮 Mes Jeux", myGamesTab);
        Tab publishersTabUI = new Tab("🏢 Éditeurs", publishersTab);
        Tab friendsTabUI = new Tab("👥 Amis", friendsTab);
        Tab wishlistTabUI = new Tab("❤ Liste de souhaits", wishlistTab);
        Tab notificationsTabUI = new Tab("🔔 Notifications", notificationsTab);

        tabs.getTabs().addAll(libraryTabUI, myGamesTabUI, publishersTabUI, friendsTabUI, wishlistTabUI, notificationsTabUI);

        // Barre du haut avec le solde
        javafx.scene.control.Label soldeTitle = new javafx.scene.control.Label("Solde:");
        soldeTitle.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold;");
        walletLabel = new javafx.scene.control.Label();
        walletLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        javafx.scene.control.Label userTitle = new javafx.scene.control.Label("Utilisateur:");
        userTitle.setStyle("-fx-text-fill: #ccc; -fx-font-weight: bold;");
        javafx.scene.control.Label userLabel = new javafx.scene.control.Label();
        userLabel.setStyle("-fx-text-fill: #ccc;");
        userLabel.setText(SessionManager.getInstance().getCurrentPlayer().getUsername());
        updateWalletLabel();

        javafx.scene.layout.HBox topBar = new javafx.scene.layout.HBox(15, soldeTitle, walletLabel, userTitle, userLabel);
        topBar.setPadding(new javafx.geometry.Insets(10));
        topBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: #1f1f1f;");

        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setTop(topBar);
        root.setCenter(tabs);

        Scene scene = new Scene(root, 950, 700);
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
        myGamesTab.refresh();
        wishlistTab.refresh();
        publishersTab.refresh();
        friendsTab.refresh();
        notificationsTab.setGames(GameDataService.getInstance().getAllGames());
        updateWalletLabel();
    }

    private void updateWalletLabel() {
        if (walletLabel != null && SessionManager.getInstance().getCurrentPlayer() != null) {
            walletLabel.setText(String.format("%.2f €", SessionManager.getInstance().getCurrentPlayer().getWallet()));
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
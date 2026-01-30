package org.example.views;

import org.example.controllers.PlayerDashboardController;
import org.example.models.Game;
import org.example.services.GameDataService;
import org.example.services.KafkaConsumerService;
import org.example.services.KafkaProducerService;
import org.example.services.SessionManager;
import org.example.views.components.dialogs.LoginDialog;
import org.example.views.components.tabs.FriendsTab;
import org.example.views.components.tabs.LibraryTab;
import org.example.views.components.tabs.MyGamesTab;
import org.example.views.components.tabs.NotificationsTab;
import org.example.views.components.tabs.PublishersTab;
import org.example.views.components.tabs.WishlistTab;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class PlayerDashboard extends Application {

    private MyGamesTab myGamesTab;
    private LibraryTab libraryTab;
    private WishlistTab wishlistTab;
    private NotificationsTab notificationsTab;
    private PublishersTab publishersTab;
    private FriendsTab friendsTab;
    private javafx.scene.control.Label walletLabel;

    private void loadUserNotifications() {
        String userId = SessionManager.getInstance().getCurrentPlayer().getId();
        notificationsTab.fetchAndDisplayNotifications(userId);
    }

    @Override
    public void start(Stage stage) {
        // Afficher la fenêtre de connexion
        if (!LoginDialog.show()) {
            return; // L'utilisateur a quitté
        }
        // Initialize controller and Kafka producer (local defaults)
        KafkaProducerService kafkaProducer = new KafkaProducerService("localhost:9092", "http://localhost:8081");
        PlayerDashboardController controller = new PlayerDashboardController(
            kafkaProducer,
            SessionManager.getInstance().getCurrentPlayer().getId(),
            SessionManager.getInstance().getCurrentPlayer().getUsername()
        );
        SessionManager.getInstance().setPlayerController(controller);
        
        // Initialize tabs before KafkaConsumerService
        libraryTab = new LibraryTab(this::onGamePurchased);
        libraryTab.setOnRefreshAll(this::refreshAll);
    
        myGamesTab = new MyGamesTab(this::refreshAll);
        wishlistTab = new WishlistTab(this::onGamePurchased);
        wishlistTab.setLibraryTab(libraryTab);
        publishersTab = new PublishersTab(this::onGamePurchased);
        friendsTab = new FriendsTab();
        notificationsTab = new NotificationsTab();

        // Initialize KafkaConsumerService and start listening to topics
        KafkaConsumerService kafkaConsumerService = new KafkaConsumerService("player-simulator-group", notificationsTab);
        kafkaConsumerService.startListeningToTopics("new-notification", "game-crash-reported");

        // Ensure Kafka consumer is stopped when the GUI is closed
        stage.setOnCloseRequest(event -> {
            kafkaConsumerService.stop();
        });

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab libraryTabUI = new Tab("Bibliothèque", libraryTab);
        Tab myGamesTabUI = new Tab("Mes Jeux", myGamesTab);
        Tab publishersTabUI = new Tab("Éditeurs", publishersTab);
        Tab friendsTabUI = new Tab("Amis", friendsTab);
        Tab wishlistTabUI = new Tab("Liste de souhaits", wishlistTab);
        Tab notificationsTabUI = new Tab("Notifications", notificationsTab);
        tabs.getTabs().addAll(libraryTabUI, myGamesTabUI, publishersTabUI, friendsTabUI, wishlistTabUI, notificationsTabUI);
        
        // Rafraîchir chaque onglet à chaque fois qu'on clique dessus
        tabs.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == libraryTabUI) {
                GameDataService.getInstance().reload();
                libraryTab.refresh();
            } else if (newTab == myGamesTabUI) {
                myGamesTab.refresh();
            } else if (newTab == publishersTabUI) {
                publishersTab.refresh();
            } else if (newTab == friendsTabUI) {
                friendsTab.refresh();
            } else if (newTab == wishlistTabUI) {
                wishlistTab.refresh();
            } else if (newTab == notificationsTabUI) {
                notificationsTab.setGames(GameDataService.getInstance().getAllGames());
                loadUserNotifications();
            }
        });

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
        try {
            GameDataService.getInstance();
        } catch (Exception e) {
            // reload() handles its own error reporting, but guard here to avoid crashing UI
            e.printStackTrace();
        }
        myGamesTab.refresh();
        libraryTab.refresh();
        wishlistTab.refresh();
        publishersTab.refresh();
        friendsTab.refresh();
        notificationsTab.setGames(GameDataService.getInstance().getAllGames());
            loadUserNotifications();
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
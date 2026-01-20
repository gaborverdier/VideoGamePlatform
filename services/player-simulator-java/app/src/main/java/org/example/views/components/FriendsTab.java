package org.example.views.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.models.Game;
import org.example.models.Player;
import org.example.services.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class FriendsTab extends VBox {
    
    private VBox friendsList;
    private TextField searchField;
    private List<Player> allPlayers;
    
    public FriendsTab() {
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #1e1e1e;");
        
        // Simuler quelques joueurs
        initSimulatedPlayers();
        
        // En-tête
        Label titleLabel = new Label("👥 Mes Amis");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Barre de recherche pour ajouter des amis
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Rechercher un joueur...");
        searchField.setPrefWidth(300);
        
        Button searchBtn = new Button("🔍 Rechercher");
        searchBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        searchBtn.setOnAction(e -> searchPlayers());
        
        searchBox.getChildren().addAll(searchField, searchBtn);
        
        // Liste des amis
        Label friendsTitle = new Label("Amis (" + SessionManager.getInstance().getCurrentPlayer().getFriendIds().size() + ")");
        friendsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1e1e1e; -fx-background-color: #1e1e1e;");
        
        friendsList = new VBox(10);
        friendsList.setPadding(new Insets(10));
        
        scrollPane.setContent(friendsList);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        getChildren().addAll(titleLabel, searchBox, new Separator(), friendsTitle, scrollPane);
        
        refresh();
    }
    
    private void initSimulatedPlayers() {
        allPlayers = new ArrayList<>();
        // Simuler quelques joueurs fictifs
        allPlayers.add(new Player("PLAYER-001", "GamerPro42", "gamerpro@email.com", 150.0));
        allPlayers.add(new Player("PLAYER-002", "NightOwl", "nightowl@email.com", 75.0));
        allPlayers.add(new Player("PLAYER-003", "SpeedRunner", "speedrunner@email.com", 200.0));
        allPlayers.add(new Player("PLAYER-004", "CasualGamer", "casual@email.com", 50.0));
        allPlayers.add(new Player("PLAYER-005", "ProLeagueX", "proleague@email.com", 300.0));
        allPlayers.add(new Player("PLAYER-006", "RetroFan", "retro@email.com", 80.0));
        allPlayers.add(new Player("PLAYER-007", "StreamerKing", "streamer@email.com", 500.0));
    }
    
    public void refresh() {
        friendsList.getChildren().clear();
        
        Player currentPlayer = SessionManager.getInstance().getCurrentPlayer();
        List<String> friendIds = currentPlayer.getFriendIds();
        
        if (friendIds.isEmpty()) {
            Label noFriends = new Label("Vous n'avez pas encore d'amis. Utilisez la recherche pour en ajouter !");
            noFriends.setStyle("-fx-text-fill: #aaa;");
            friendsList.getChildren().add(noFriends);
        } else {
            for (String friendId : friendIds) {
                Player friend = findPlayerById(friendId);
                if (friend != null) {
                    friendsList.getChildren().add(createFriendCard(friend, true));
                }
            }
        }
    }
    
    private void searchPlayers() {
        String query = searchField.getText().toLowerCase().trim();
        
        if (query.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Entrez un nom de joueur à rechercher.");
            alert.showAndWait();
            return;
        }
        
        Player currentPlayer = SessionManager.getInstance().getCurrentPlayer();
        List<Player> results = new ArrayList<>();
        
        for (Player player : allPlayers) {
            if (player.getUsername().toLowerCase().contains(query) 
                && !player.getId().equals(currentPlayer.getId())) {
                results.add(player);
            }
        }
        
        showSearchResults(results);
    }
    
    private void showSearchResults(List<Player> results) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Résultats de recherche");
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("Joueurs trouvés (" + results.size() + ")");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        VBox resultsList = new VBox(10);
        
        if (results.isEmpty()) {
            Label noResults = new Label("Aucun joueur trouvé.");
            noResults.setStyle("-fx-text-fill: #aaa;");
            resultsList.getChildren().add(noResults);
        } else {
            Player currentPlayer = SessionManager.getInstance().getCurrentPlayer();
            for (Player player : results) {
                boolean isFriend = currentPlayer.getFriendIds().contains(player.getId());
                HBox playerCard = createSearchResultCard(player, isFriend, dialog);
                resultsList.getChildren().add(playerCard);
            }
        }
        
        Button closeBtn = new Button("Fermer");
        closeBtn.setOnAction(e -> dialog.close());
        
        root.getChildren().addAll(titleLabel, new Separator(), resultsList, closeBtn);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 400, 350);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private HBox createSearchResultCard(Player player, boolean isFriend, javafx.stage.Stage dialog) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
        
        Label nameLabel = new Label("👤 " + player.getUsername());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button actionBtn;
        if (isFriend) {
            actionBtn = new Button("✓ Ami");
            actionBtn.setStyle("-fx-background-color: #666; -fx-text-fill: white;");
            actionBtn.setDisable(true);
        } else {
            actionBtn = new Button("+ Ajouter");
            actionBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
            actionBtn.setOnAction(e -> {
                SessionManager.getInstance().getCurrentPlayer().addFriend(player.getId());
                actionBtn.setText("✓ Ami");
                actionBtn.setDisable(true);
                actionBtn.setStyle("-fx-background-color: #666; -fx-text-fill: white;");
                refresh();
            });
        }
        
        Button profileBtn = new Button("👁 Profil");
        profileBtn.setOnAction(e -> showPlayerProfile(player));
        
        card.getChildren().addAll(nameLabel, spacer, profileBtn, actionBtn);
        
        return card;
    }
    
    private VBox createFriendCard(Player friend, boolean canRemove) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #2d2d2d; -fx-background-radius: 8;");
        
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label avatarLabel = new Label("👤");
        avatarLabel.setStyle("-fx-font-size: 24px;");
        
        VBox info = new VBox(2);
        Label nameLabel = new Label(friend.getUsername());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label gamesLabel = new Label("🎮 " + friend.getOwnedGamesCount() + " jeux");
        gamesLabel.setStyle("-fx-text-fill: #aaa;");
        
        info.getChildren().addAll(nameLabel, gamesLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox buttons = new HBox(10);
        
        Button profileBtn = new Button("👁 Voir profil");
        profileBtn.setOnAction(e -> showPlayerProfile(friend));
        
        if (canRemove) {
            Button removeBtn = new Button("✖ Retirer");
            removeBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            removeBtn.setOnAction(e -> {
                SessionManager.getInstance().getCurrentPlayer().getFriendIds().remove(friend.getId());
                refresh();
            });
            buttons.getChildren().addAll(profileBtn, removeBtn);
        } else {
            buttons.getChildren().add(profileBtn);
        }
        
        header.getChildren().addAll(avatarLabel, info, spacer, buttons);
        
        card.getChildren().add(header);
        
        return card;
    }
    
    private void showPlayerProfile(Player player) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Profil de " + player.getUsername());
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        // En-tête du profil
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label avatarLabel = new Label("👤");
        avatarLabel.setStyle("-fx-font-size: 48px;");
        
        VBox userInfo = new VBox(5);
        Label nameLabel = new Label(player.getUsername());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label emailLabel = new Label("📧 " + player.getEmail());
        emailLabel.setStyle("-fx-text-fill: #aaa;");
        
        userInfo.getChildren().addAll(nameLabel, emailLabel);
        header.getChildren().addAll(avatarLabel, userInfo);
        
        // Statistiques
        VBox stats = new VBox(10);
        stats.setPadding(new Insets(15));
        stats.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 8;");
        
        Label statsTitle = new Label("📊 Statistiques");
        statsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label gamesCountLabel = new Label("🎮 Jeux possédés: " + player.getOwnedGamesCount());
        gamesCountLabel.setStyle("-fx-text-fill: white;");
        
        int totalPlaytime = player.getOwnedGames().stream().mapToInt(Game::getPlayedTime).sum();
        Label playtimeLabel = new Label("⏱ Temps de jeu total: " + (totalPlaytime / 60) + "h " + (totalPlaytime % 60) + "min");
        playtimeLabel.setStyle("-fx-text-fill: white;");
        
        stats.getChildren().addAll(statsTitle, gamesCountLabel, playtimeLabel);
        
        // Liste des jeux
        Label gamesTitle = new Label("🎮 Jeux de " + player.getUsername());
        gamesTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setStyle("-fx-background: #2b2b2b; -fx-background-color: #2b2b2b;");
        
        VBox gamesList = new VBox(5);
        gamesList.setPadding(new Insets(5));
        
        if (player.getOwnedGames().isEmpty()) {
            Label noGames = new Label("Aucun jeu possédé.");
            noGames.setStyle("-fx-text-fill: #aaa;");
            gamesList.getChildren().add(noGames);
        } else {
            for (Game game : player.getOwnedGames()) {
                HBox gameItem = new HBox(10);
                gameItem.setPadding(new Insets(5));
                gameItem.setAlignment(Pos.CENTER_LEFT);
                
                Label gameNameLabel = new Label("• " + game.getName());
                gameNameLabel.setStyle("-fx-text-fill: white;");
                
                Label gameTimeLabel = new Label("(" + game.getPlayedTime() + " min)");
                gameTimeLabel.setStyle("-fx-text-fill: #aaa;");
                
                gameItem.getChildren().addAll(gameNameLabel, gameTimeLabel);
                gamesList.getChildren().add(gameItem);
            }
        }
        
        scrollPane.setContent(gamesList);
        
        Button closeBtn = new Button("Fermer");
        closeBtn.setOnAction(e -> dialog.close());
        
        root.getChildren().addAll(header, stats, gamesTitle, scrollPane, closeBtn);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 450, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private Player findPlayerById(String id) {
        return allPlayers.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
}

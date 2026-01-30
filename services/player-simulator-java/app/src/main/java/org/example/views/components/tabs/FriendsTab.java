package org.example.views.components.tabs;

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
    private Label friendsTitle;
    
    public FriendsTab() {
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #1e1e1e;");
        
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
        friendsTitle = new Label("Amis (" + SessionManager.getInstance().getCurrentPlayer().getFriendIds().size() + ")");
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
    
    
    public void refresh() {
        friendsList.getChildren().clear();
        
        Player currentPlayer = SessionManager.getInstance().getCurrentPlayer();
        List<String> friendIds = currentPlayer.getFriendIds();
        
        // Mettre à jour le compteur d'amis
        if (friendsTitle != null) {
            friendsTitle.setText("Amis (" + friendIds.size() + ")");
        }
        
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
        HBox card = new HBox(10);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
        
        Label nameLabel = new Label(player.getUsername());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button actionBtn = new Button(isFriend ? "Déjà ami" : "Ajouter");
        actionBtn.setDisable(isFriend);
        actionBtn.setStyle(isFriend ? "-fx-background-color: #555; -fx-text-fill: #aaa;" : "-fx-background-color: #4CAF50; -fx-text-fill: white;");
        actionBtn.setOnAction(e -> {
            SessionManager.getInstance().getCurrentPlayer().addFriend(player.getId());
            actionBtn.setText("Déjà ami");
            actionBtn.setDisable(true);
            actionBtn.setStyle("-fx-background-color: #555; -fx-text-fill: #aaa;");
            refresh();
        });
        
        card.getChildren().addAll(nameLabel, spacer, actionBtn);
        return card;
    }
    
    private HBox createFriendCard(Player friend, boolean canRemove) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
        
        VBox infoBox = new VBox(5);
        
        Label nameLabel = new Label(friend.getUsername());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label gamesLabel = new Label(friend.getOwnedGames().size() + " jeux possédés");
        gamesLabel.setStyle("-fx-text-fill: #aaa;");
        
        // Jeux en commun
        int commonGames = countCommonGames(friend);
        Label commonLabel = new Label(commonGames + " jeu(x) en commun");
        commonLabel.setStyle("-fx-text-fill: #4CAF50;");
        
        infoBox.getChildren().addAll(nameLabel, gamesLabel, commonLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        VBox actionsBox = new VBox(5);
        
        Button viewBtn = new Button("👁 Voir profil");
        viewBtn.setOnAction(e -> showFriendProfile(friend));
        
        Button removeBtn = new Button("❌ Retirer");
        removeBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
        removeBtn.setOnAction(e -> {
            SessionManager.getInstance().getCurrentPlayer().removeFriend(friend.getId());
            refresh();
        });
        
        actionsBox.getChildren().addAll(viewBtn, removeBtn);
        
        card.getChildren().addAll(infoBox, spacer, actionsBox);
        return card;
    }
    
    private int countCommonGames(Player friend) {
        Player current = SessionManager.getInstance().getCurrentPlayer();
        int count = 0;
        for (Game game : current.getOwnedGames()) {
            if (friend.getOwnedGames().stream().anyMatch(g -> g.getId().equals(game.getId()))) {
                count++;
            }
        }
        return count;
    }
    
    private void showFriendProfile(Player friend) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Profil - " + friend.getUsername());
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label nameLabel = new Label(friend.getUsername());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label gamesTitle = new Label("Jeux possédés (" + friend.getOwnedGames().size() + ")");
        gamesTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        VBox gamesList = new VBox(5);
        for (Game game : friend.getOwnedGames()) {
            Label gameLabel = new Label("• " + game.getName() + " (" + game.getPlayedTime() + " min)");
            gameLabel.setStyle("-fx-text-fill: #aaa;");
            gamesList.getChildren().add(gameLabel);
        }
        
        if (friend.getOwnedGames().isEmpty()) {
            Label noGames = new Label("Aucun jeu");
            noGames.setStyle("-fx-text-fill: #aaa;");
            gamesList.getChildren().add(noGames);
        }
        
        Button closeBtn = new Button("Fermer");
        closeBtn.setOnAction(e -> dialog.close());
        
        root.getChildren().addAll(nameLabel, new Separator(), gamesTitle, gamesList, closeBtn);
        
        javafx.scene.Scene scene = new javafx.scene.Scene(root, 400, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private Player findPlayerById(String id) {
        for (Player player : allPlayers) {
            if (player.getId().equals(id)) {
                return player;
            }
        }
        return null;
    }
}

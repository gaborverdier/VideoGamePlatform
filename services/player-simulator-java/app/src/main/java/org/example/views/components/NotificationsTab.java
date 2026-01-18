package org.example.views.components;

import javafx.geometry. Insets;
import javafx.scene.control.*;
import javafx.scene. layout.*;
import org.example.models.Game;

import java.util.ArrayList;
import java.util.List;

public class NotificationsTab extends ScrollPane {
    
    private VBox notifList;
    private List<Game> allGames;
    
    public NotificationsTab() {
        this.allGames = new ArrayList<>();
        
        notifList = new VBox(10);
        notifList.setPadding(new Insets(20));
        notifList.setStyle("-fx-background-color: #2b2b2b;");
        
        updateNotifications();
        
        this.setContent(notifList);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: #2b2b2b;");
    }
    
    public void setGames(List<Game> games) {
        this.allGames = games;
        updateNotifications();
    }
    
    private void updateNotifications() {
        notifList.getChildren().clear();
        
        // Favoris en premier (bandeau rouge)
        Label favHeader = new Label("⭐ Jeux Favoris");
        favHeader. setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10;");
        favHeader.setMaxWidth(Double.MAX_VALUE);
        
        boolean hasFavorites = false;
        for (Game game : allGames) {
            if (game.isFavorite() && (! game.getAvailableUpdates().isEmpty() || !game.getAvailableDLCs().isEmpty())) {
                if (! hasFavorites) {
                    notifList.getChildren().add(favHeader);
                    hasFavorites = true;
                }
                notifList.getChildren().add(createNotifCard(game));
            }
        }
        
        // Autres jeux
        Label otherHeader = new Label("📢 Autres notifications");
        otherHeader.setStyle("-fx-background-color:  #555; -fx-text-fill:  white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10;");
        otherHeader.setMaxWidth(Double.MAX_VALUE);
        
        boolean hasOthers = false;
        for (Game game : allGames) {
            if (!game.isFavorite() && (!game.getAvailableUpdates().isEmpty() || !game.getAvailableDLCs().isEmpty())) {
                if (!hasOthers) {
                    notifList.getChildren().add(otherHeader);
                    hasOthers = true;
                }
                notifList.getChildren().add(createNotifCard(game));
            }
        }
        
        if (!hasFavorites && !hasOthers) {
            Label empty = new Label("Aucune notification pour le moment.");
            empty.setStyle("-fx-text-fill: #aaa; -fx-font-size: 16px;");
            notifList.getChildren().add(empty);
        }
    }
    
    private VBox createNotifCard(Game game) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
        
        Label gameLabel = new Label(game.getName());
        gameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
        
        card.getChildren().add(gameLabel);
        
        for (String update : game.getAvailableUpdates()) {
            Label updateLabel = new Label("⬇ MAJ: " + update);
            updateLabel.setStyle("-fx-text-fill: #4CAF50;");
            card.getChildren().add(updateLabel);
        }
        
        for (String dlc : game.getAvailableDLCs()) {
            Label dlcLabel = new Label("🎁 DLC: " + dlc);
            dlcLabel.setStyle("-fx-text-fill: #FF9800;");
            card.getChildren().add(dlcLabel);
        }
        
        return card;
    }
}
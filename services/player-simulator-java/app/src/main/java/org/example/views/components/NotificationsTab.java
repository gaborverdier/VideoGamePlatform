package org.example.views.components;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx. scene.layout.*;
import org.example.models.Game;
import org.example.models. Notification;

import java.util.ArrayList;
import java.util. Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationsTab extends ScrollPane {
    
    private VBox notifList;
    private List<Game> allGames;
    private List<Notification> notifications;
    
    public NotificationsTab() {
        this.allGames = new ArrayList<>();
        this.notifications = new ArrayList<>();
        
        notifList = new VBox(10);
        notifList. setPadding(new Insets(20));
        notifList.setStyle("-fx-background-color:  #2b2b2b;");
        
        updateNotifications();
        
        this.setContent(notifList);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: #2b2b2b;");
    }
    
    public void setGames(List<Game> games) {
        this.allGames = games;
        generateNotifications();
        updateNotifications();
    }
    
    private void generateNotifications() {
        notifications. clear();
        
        // Générer des notifications pour les MAJ/DLC
        for (Game game : allGames) {
            if (!game.isOwned()) continue;
            
            boolean isFav = game.isFavorite();
            
            for (String update : game.getPendingUpdates()) {
                notifications.add(new Notification(
                    Notification.Type. GAME_UPDATE,
                    game.getName(),
                    "Nouvelle mise à jour disponible : " + update,
                    isFav,
                    game.getId()
                ));
            }
            
            // CORRECTION ICI : utiliser DLC au lieu de String
            for (Game. DLC dlc : game.getPendingDLCs()) {
                notifications.add(new Notification(
                    Notification.Type. GAME_DLC,
                    game.getName(),
                    "Nouveau DLC disponible : " + dlc.getName() + " - " + dlc. getFormattedPrice(),
                    isFav,
                    game. getId()
                ));
            }
        }
        
        // Générer des notifications pour les jeux wishlistés
        for (Game game : allGames) {
            if (!game.isWishlisted()) continue;
            
            // Baisse de prix pour les jeux wishlistés
            if (game.getPrice() < 50 && !game.isOwned()) {
                notifications.add(new Notification(
                    Notification.Type.PRICE_DROP,
                    game.getName(),
                    "Baisse de prix ! Prix actuel : " + game.getFormattedPrice(),
                    false,
                    game.getId()
                ));
            }
            
            // Nouveaux avis pour les jeux wishlistés
            if (!game.getReviews().isEmpty()) {
                notifications.add(new Notification(
                    Notification.Type.NEW_REVIEW,
                    game.getName(),
                    game.getReviews().size() + " avis disponible(s)",
                    false,
                    game.getId()
                ));
            }
        }
    }
    
    private void updateNotifications() {
        notifList.getChildren().clear();
        
        if (notifications.isEmpty()) {
            Label empty = new Label("Aucune notification pour le moment.");
            empty.setStyle("-fx-text-fill: #aaa; -fx-font-size: 16px;");
            notifList.getChildren().add(empty);
            return;
        }
        
        // Séparer favoris et autres
        List<Notification> favNotifs = notifications.stream()
            .filter(Notification::isFromFavorite)
            .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
            .collect(Collectors.toList());
            
        List<Notification> otherNotifs = notifications.stream()
            .filter(n -> !n.isFromFavorite())
            .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
            .collect(Collectors.toList());
        
        // Favoris en premier (bandeau rouge)
        if (!favNotifs.isEmpty()) {
            Label favHeader = new Label("⭐ Jeux Favoris");
            favHeader. setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10;");
            favHeader.setMaxWidth(Double.MAX_VALUE);
            notifList.getChildren().add(favHeader);
            
            for (Notification notif : favNotifs) {
                notifList.getChildren().add(createNotifCard(notif));
            }
        }
        
        // Autres notifications
        if (!otherNotifs.isEmpty()) {
            Label otherHeader = new Label("���� Autres notifications");
            otherHeader.setStyle("-fx-background-color:  #555; -fx-text-fill:  white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10;");
            otherHeader.setMaxWidth(Double.MAX_VALUE);
            notifList.getChildren().add(otherHeader);
            
            for (Notification notif : otherNotifs) {
                notifList.getChildren().add(createNotifCard(notif));
            }
        }
    }
    
    private VBox createNotifCard(Notification notif) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #3c3c3c; -fx-background-radius: 5;");
        
        Label titleLabel = new Label(notif.getIcon() + " " + notif.getTitle());
        titleLabel. setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");
        
        Label messageLabel = new Label(notif. getMessage());
        messageLabel.setStyle("-fx-text-fill:  #aaa;");
        
        card.getChildren().addAll(titleLabel, messageLabel);
        
        return card;
    }
    
    public void refresh() {
        generateNotifications();
        updateNotifications();
    }
}
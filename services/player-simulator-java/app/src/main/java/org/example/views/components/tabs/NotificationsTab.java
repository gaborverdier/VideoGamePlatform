package org.example.views.components.tabs;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.example.models.Game;
import org.example.models.Notification;
import org.example.services.NotificationService;

import com.gaming.api.models.NotificationModel;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class NotificationsTab extends ScrollPane {

    public void fetchAndDisplayNotifications(String userId) {
        new Thread(() -> {
            List<NotificationModel> models = NotificationService.fetchUserNotifications(userId);
            List<Notification> notifs = new ArrayList<>();
            for (NotificationModel model : models) {
                notifs.add(convertModelToNotification(model));
            }
            Platform.runLater(() -> {
                loadNotifications(notifs);
            });
        }).start();
    }

    private Notification convertModelToNotification(NotificationModel model) {
        // Map NotificationModel fields to Notification fields
        // Adjust mapping as needed for your schema
        return new Notification(
            model.getNotificationId(),
            Notification.Type.GAME_UPDATE, // TODO: map type if available
            model.getDescription(), // or model.getTitle() if available
            model.getDescription(),
            model.getDate(),
            false, // TODO: map favorite if available
            model.getUserId() // or model.getRelatedGameId() if available
        );
    }
    
    private VBox notifList;
    private List<Game> allGames;
    private List<Notification> notifications;
    public NotificationsTab() {
        this.allGames = new ArrayList<>();
        this.notifications = new ArrayList<>();

        notifList = new VBox(10);
        notifList.setPadding(new Insets(20));
        notifList.setStyle("-fx-background-color: #2b2b2b;");

        updateNotifications();

        this.setContent(notifList);
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: #2b2b2b;");
    }

    /**
     * Load notifications from a list of Notification objects (e.g., from JSON)
     */
    public void loadNotifications(List<Notification> notifs) {
        this.notifications.clear();
        if (notifs != null) {
            this.notifications.addAll(notifs);
        }
        updateNotifications();
    }

    // Example usage:
    // List<Notification> notifList = new ArrayList<>();
    // notifList.add(new Notification(Notification.Type.GAME_UPDATE, "Test", "this is a test 2", false, null));
    // notificationsTab.loadNotifications(notifList);
    
    public void setGames(List<Game> games) {
        this.allGames = games;
        generateNotifications();
        updateNotifications();
    }
    
    private void generateNotifications() {
        notifications.clear();
        
        // Générer des notifications pour les MAJ/DLC
        for (Game game : allGames) {
            if (!game.isOwned()) continue;
            
            boolean isFav = game.isFavorite();
            
            for (String update : game.getPendingUpdates()) {
                notifications.add(new Notification(
                    Notification.Type.GAME_UPDATE,
                    game.getName(),
                    "Nouvelle mise à jour disponible : " + update,
                    Instant.now().toEpochMilli(),
                    isFav,
                    game.getId()
                ));
            }
            
            for (Game.DLC dlc : game.getPendingDLCs()) {
                notifications.add(new Notification(
                    Notification.Type.GAME_DLC,
                    game.getName(),
                    "Nouveau DLC disponible : " + dlc.getName() + " - " + dlc.getFormattedPrice(),
                    Instant.now().toEpochMilli(),
                    isFav,
                    game.getId()
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
                    Instant.now().toEpochMilli(),
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
                    Instant.now().toEpochMilli(),
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
            favHeader.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10;");
            favHeader.setMaxWidth(Double.MAX_VALUE);
            notifList.getChildren().add(favHeader);
            
            for (Notification notif : favNotifs) {
                notifList.getChildren().add(createNotifCard(notif));
            }
        }
        
        // Autres notifications
        if (!otherNotifs.isEmpty()) {
            Label otherHeader = new Label("🔔 Notifications");
            otherHeader.setStyle("-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10;");
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
        
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label iconLabel = new Label(notif.getIcon());
        iconLabel.setStyle("-fx-font-size: 18px;");
        
        Label titleLabel = new Label(notif.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        
        header.getChildren().addAll(iconLabel, titleLabel);
        
        Label messageLabel = new Label(notif.getMessage());
        messageLabel.setStyle("-fx-text-fill: #aaa;");
        messageLabel.setWrapText(true);
        
        // Date and time label
        Label dateLabel = new Label();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (notif.getCreatedAt() != null) {
            dateLabel.setText(formatter.format(notif.getCreatedAt()));
        } else {
            dateLabel.setText("");
        }
        dateLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");

        card.getChildren().addAll(header, messageLabel, dateLabel);
        
        return card;
    }

    public void addNotificationToBeginning(Notification notification) {
        this.notifications.add(0, notification); // Add to the beginning of the list
        // Remove duplicates by ID, keeping the first occurrence
        List<String> seenIds = new ArrayList<>();
        this.notifications = this.notifications.stream()
            .filter(n -> {
                String id = n.getId();
                if (id == null) return true;
                if (seenIds.contains(id)) return false;
                seenIds.add(id);
                return true;
            })
            .collect(Collectors.toList());

        // print notifications
        for (Notification n : notifications) {
            System.out.println("Notification ID: " + n.getId() + ", Title: " + n.getTitle());
        }
        updateNotifications(); // Refresh the UI
    }
}

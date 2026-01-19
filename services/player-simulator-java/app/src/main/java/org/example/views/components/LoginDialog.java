package org.example.views. components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene. control.*;
import javafx.scene.layout.*;
import javafx.stage. Modality;
import javafx. stage.Stage;
import org.example.models.Player;
import org.example.services.SessionManager;

public class LoginDialog {
    
    public static boolean show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Connexion");
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("🎮 Plateforme de Jeux");
        titleLabel. setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Toggle entre connexion et création
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton loginMode = new RadioButton("Se connecter");
        loginMode.setToggleGroup(modeGroup);
        loginMode.setSelected(true);
        loginMode.setStyle("-fx-text-fill: white;");
        
        RadioButton registerMode = new RadioButton("Créer un compte");
        registerMode.setToggleGroup(modeGroup);
        registerMode.setStyle("-fx-text-fill:  white;");
        
        HBox modeBox = new HBox(15);
        modeBox.setAlignment(Pos.CENTER);
        modeBox.getChildren().addAll(loginMode, registerMode);
        
        // Formulaire
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER);
        
        Label usernameLabel = new Label("Nom d'utilisateur:");
        usernameLabel.setStyle("-fx-text-fill: white;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Votre pseudo");
        
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-text-fill: white;");
        TextField emailField = new TextField();
        emailField.setPromptText("votre@email.com");
        
        Label passwordLabel = new Label("Mot de passe:");
        passwordLabel.setStyle("-fx-text-fill: white;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Votre mot de passe");
        
        Label loginInfoLabel = new Label("Username OU Email:");
        loginInfoLabel. setStyle("-fx-text-fill: white;");
        TextField loginField = new TextField();
        loginField.setPromptText("Pseudo ou email");
        
        // Affichage conditionnel selon le mode
        form.add(usernameLabel, 0, 0);
        form.add(usernameField, 1, 0);
        form.add(emailLabel, 0, 1);
        form.add(emailField, 1, 1);
        form.add(passwordLabel, 0, 2);
        form.add(passwordField, 1, 2);
        
        // Changer le formulaire selon le mode
        modeGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            form.getChildren().clear();
            
            if (newVal == registerMode) {
                // Création :  username + email + password
                form.add(usernameLabel, 0, 0);
                form.add(usernameField, 1, 0);
                form.add(emailLabel, 0, 1);
                form.add(emailField, 1, 1);
                form.add(passwordLabel, 0, 2);
                form.add(passwordField, 1, 2);
            } else {
                // Connexion : username OU email + password
                form.add(loginInfoLabel, 0, 0);
                form.add(loginField, 1, 0);
                form.add(passwordLabel, 0, 1);
                form.add(passwordField, 1, 1);
            }
        });
        
        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button actionBtn = new Button("Se connecter");
        actionBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        actionBtn.setOnAction(e -> {
            if (registerMode.isSelected()) {
                // Création de compte - besoin des 3 champs
                if (usernameField.getText().isEmpty() || emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert. setContentText("Veuillez remplir tous les champs !");
                    alert.showAndWait();
                } else {
                    Player player = new Player(usernameField.getText(), emailField.getText());
                    SessionManager.getInstance().login(player);
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Compte créé");
                    success.setContentText("Bienvenue " + player.getUsername() + " !\nVous avez 100€ sur votre compte.");
                    success.showAndWait();
                    
                    dialog.close();
                }
            } else {
                // Connexion - besoin de (username OU email) + password
                if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Veuillez remplir tous les champs !");
                    alert.showAndWait();
                } else {
                    // Pour l'instant on simule juste la connexion
                    Player player = new Player(loginField. getText(), "user@email.com");
                    SessionManager.getInstance().login(player);
                    
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Connexion réussie");
                    success.setContentText("Bon retour " + player.getUsername() + " !");
                    success.showAndWait();
                    
                    dialog.close();
                }
            }
        });
        
        // Changer le texte du bouton selon le mode
        modeGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal == registerMode) {
                actionBtn. setText("Créer mon compte");
            } else {
                actionBtn.setText("Se connecter");
            }
        });
        
        Button quitBtn = new Button("Quitter");
        quitBtn.setOnAction(e -> {
            dialog.close();
            System.exit(0);
        });
        
        buttonBox. getChildren().addAll(actionBtn, quitBtn);
        
        root.getChildren().addAll(titleLabel, modeBox, form, buttonBox);
        
        Scene scene = new Scene(root, 450, 350);
        dialog.setScene(scene);
        dialog.showAndWait();
        
        return SessionManager.getInstance().isLoggedIn();
    }
}
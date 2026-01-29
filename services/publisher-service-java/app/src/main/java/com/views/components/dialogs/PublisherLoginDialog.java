package com.views.components.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PublisherLoginDialog {
    
    public static String[] show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Connexion Éditeur");
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("Plateforme Éditeurs");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Mode: se connecter ou créer un compte
        Label modeLabel = new Label("Mode:");
        modeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton loginMode = new RadioButton("Se connecter");
        loginMode.setToggleGroup(modeGroup);
        loginMode.setSelected(true);
        loginMode.setStyle("-fx-text-fill: white;");

        RadioButton createMode = new RadioButton("Créer un compte");
        createMode.setToggleGroup(modeGroup);
        createMode.setStyle("-fx-text-fill: white;");

        HBox modeBox = new HBox(15);
        modeBox.setAlignment(Pos.CENTER);
        modeBox.getChildren().addAll(loginMode, createMode);

        // Choix du type d'éditeur
        Label typeLabel = new Label("Type d'éditeur:");
        typeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        
        ToggleGroup typeGroup = new ToggleGroup();
        RadioButton companyButton = new RadioButton("Entreprise");
        companyButton.setToggleGroup(typeGroup);
        companyButton.setSelected(true);
        companyButton.setStyle("-fx-text-fill: white;");
        
        RadioButton independentButton = new RadioButton("Indépendant");
        independentButton.setToggleGroup(typeGroup);
        independentButton.setStyle("-fx-text-fill: white;");
        
        HBox typeBox = new HBox(15);
        typeBox.setAlignment(Pos.CENTER);
        typeBox.getChildren().addAll(companyButton, independentButton);
        
        // Formulaire
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER);
        
        Label nameLabel = new Label("Nom:");
        nameLabel.setStyle("-fx-text-fill: white;");
        TextField nameField = new TextField();
        nameField.setPromptText("Nom de l'entreprise ou votre nom");
        nameField.setPrefWidth(300);
        
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-text-fill: white;");
        TextField emailField = new TextField();
        emailField.setPromptText("votre@email.com");
        emailField.setPrefWidth(300);
        
        Label passwordLabel = new Label("Mot de passe:");
        passwordLabel.setStyle("-fx-text-fill: white;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Votre mot de passe");
        passwordField.setPrefWidth(300);
        
        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);
        form.add(emailLabel, 0, 1);
        form.add(emailField, 1, 1);
        form.add(passwordLabel, 0, 2);
        form.add(passwordField, 1, 2);
        
        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button loginButton = new Button("Connexion");
        loginButton.setPrefWidth(100);
        loginButton.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        
        Button cancelButton = new Button("Annuler");
        cancelButton.setPrefWidth(100);
        cancelButton.setStyle("-fx-font-size: 12px; -fx-padding: 10px;");
        
        buttonBox.getChildren().addAll(loginButton, cancelButton);
        
        root.getChildren().addAll(titleLabel, new Separator(), modeLabel, modeBox, typeLabel, typeBox, form, buttonBox);
        
        Scene scene = new Scene(root, 500, 400);
        dialog.setScene(scene);
        
        String[] result = {null, null, null}; // [name, email, type]
        
        // Change text depending on mode
        modeGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            if (newV == createMode) loginButton.setText("Créer le compte");
            else loginButton.setText("Connexion");
        });

        loginButton.setOnAction(e -> {
            if (!nameField.getText().isEmpty() && !emailField.getText().isEmpty()) {
                result[0] = nameField.getText();
                result[1] = emailField.getText();
                result[2] = companyButton.isSelected() ? "COMPANY" : "INDEPENDENT";
                dialog.close();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Champs manquants");
                alert.setContentText("Veuillez remplir tous les champs");
                alert.showAndWait();
            }
        });
        
        cancelButton.setOnAction(e -> dialog.close());
        
        dialog.showAndWait();
        return result;
    }
}

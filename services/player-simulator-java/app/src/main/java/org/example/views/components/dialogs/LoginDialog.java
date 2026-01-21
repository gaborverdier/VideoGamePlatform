package org.example.views.components.dialogs;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.specific.SpecificRecordBase;
import org.example.models.Player;
import org.example.services.SessionManager;
import org.example.util.ApiClient;
import org.example.util.AvroJacksonConfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaming.api.requests.UserRegistrationRequest;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginDialog {
    
    public static boolean show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Connexion");
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2b2b2b;");
        
        Label titleLabel = new Label("Plateforme de Jeux");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        // Toggle entre connexion et création de compte
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton loginMode = new RadioButton("Se connecter");
        loginMode.setToggleGroup(modeGroup);
        loginMode.setSelected(true);
        loginMode.setStyle("-fx-text-fill: white;");
        
        RadioButton registerMode = new RadioButton("Créer un compte");
        registerMode.setToggleGroup(modeGroup);
        registerMode.setStyle("-fx-text-fill: white;");
        
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
        
        Label loginInfoLabel = new Label("Pseudo/Email:");
        loginInfoLabel.setStyle("-fx-text-fill: white;");
        TextField loginField = new TextField();
        loginField.setPromptText("Pseudo ou email");

        Label lastName = new Label("Nom :");
        lastName.setStyle("-fx-text-fill: white;");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("DUPONT");

        Label firstName = new Label("Prénom :");
        firstName.setStyle("-fx-text-fill: white;");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Martin");

        Label birthDate = new Label("Date de naissance :");
        birthDate.setStyle("-fx-text-fill: white;");
        TextField birthDateField = new TextField();
        birthDateField.setPromptText("01/01/1990");
        
        // Affichage initial pour le mode connexion (sélectionné par défaut)
        form.add(loginInfoLabel, 0, 0);
        form.add(loginField, 1, 0);
        form.add(passwordLabel, 0, 1);
        form.add(passwordField, 1, 1);
        
        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button actionBtn = new Button("Se connecter");
        actionBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // Listener pour changer de mode (connexion/création)
        modeGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            form.getChildren().clear();
            
            if (newVal == registerMode) {
                form.add(lastName, 0, 0);
                form.add(lastNameField, 1, 0);
                form.add(firstName, 0, 1);
                form.add(firstNameField, 1, 1);
                form.add(birthDate, 0, 2);
                form.add(birthDateField, 1, 2);
                form.add(usernameLabel, 0, 3);
                form.add(usernameField, 1, 3);
                form.add(emailLabel, 0, 4);
                form.add(emailField, 1, 4);
                form.add(passwordLabel, 0, 5);
                form.add(passwordField, 1, 5);
                actionBtn.setText("Créer mon compte");
            } else {
                form.add(loginInfoLabel, 0, 0);
                form.add(loginField, 1, 0);
                form.add(passwordLabel, 0, 1);
                form.add(passwordField, 1, 1);
                actionBtn.setText("Se connecter");
            }
        });

        // Appui sur le bouton de validation --> vérification
        actionBtn.setOnAction(e -> {
            if (registerMode.isSelected()) {
                // Création de compte - besoin des 3 champs
                if (lastNameField.getText().isEmpty() || firstNameField.getText().isEmpty() || birthDateField.getText().isEmpty() || usernameField.getText().isEmpty() || emailField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Veuillez remplir tous les champs !");
                    alert.showAndWait();
                } else {
                    try {
                        // Populate Avro-generated UserRegistrationRequest
                        UserRegistrationRequest req = new UserRegistrationRequest();
                        req.setUsername(usernameField.getText());
                        req.setEmail(emailField.getText());
                        req.setPassword(passwordField.getText());
                        req.setCountry("N/A"); // TODO add country field
                        // Use centralized Avro ObjectMapper
                        String json = AvroJacksonConfig.avroObjectMapper().writeValueAsString(req);
                        // Use centralized API client
                        ApiClient.postJson("/api/users/register", json);
                        // Registration successful
                        Player player = new Player(usernameField.getText(), emailField.getText());
                        SessionManager.getInstance().login(player);
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Compte créé");
                        success.setContentText("Bienvenue " + player.getUsername() + " !\nVous avez 100€ sur votre compte.");
                        success.showAndWait();
                        dialog.close();
                    } catch (Exception ex) {
                        System.out.println("[REGISTER] Error during account creation:");
                        ex.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Erreur lors de la création du compte: " + ex.getMessage());
                        alert.showAndWait();
                    }
                }
            } else {
                // Connexion - besoin de (username OU email) + password
                if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Veuillez remplir tous les champs !");
                    alert.showAndWait();
                } else {
                    try {
                        // Prepare login request
                        Map<String, String> loginReq = new HashMap<>();
                        loginReq.put("username", loginField.getText());
                        loginReq.put("password", passwordField.getText());
                        String json = AvroJacksonConfig.avroObjectMapper().writeValueAsString(loginReq);
                        String responseJson = ApiClient.postJson("/api/auth/login", json);
                        ObjectMapper mapper = AvroJacksonConfig.avroObjectMapper();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> resp = mapper.readValue(responseJson, Map.class);
                        if (resp.containsKey("userId")) {
                            Player player = new Player((String) resp.get("username"), "");
                            SessionManager.getInstance().login(player);
                            Alert success = new Alert(Alert.AlertType.INFORMATION);
                            success.setTitle("Connexion réussie");
                            success.setContentText("Bon retour " + player.getUsername() + " !");
                            success.showAndWait();
                            dialog.close();
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setContentText("Erreur de connexion: " + resp.getOrDefault("error", ""));
                            alert.showAndWait();
                        }
                    } catch (Exception ex) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Erreur technique: " + ex.getMessage());
                        alert.showAndWait();
                    }
                }
            }
        });
        
        Button quitBtn = new Button("Quitter");
        quitBtn.setOnAction(e -> {
            dialog.close();
            System.exit(0);
        });
        
        buttonBox.getChildren().addAll(actionBtn, quitBtn);
        
        root.getChildren().addAll(titleLabel, modeBox, form, buttonBox);
        
        Scene scene = new Scene(root, 450, 350);
        dialog.setScene(scene);
        dialog.showAndWait();
        
        return SessionManager.getInstance().isLoggedIn();
    }

    // Jackson config to ignore Avro schema properties (like in platform-service)
    public static ObjectMapper avroObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.addMixIn(SpecificRecordBase.class, IgnoreAvroProperties.class);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    @JsonIgnoreProperties({ "schema", "specificData", "classSchema", "conversion" })
    public abstract static class IgnoreAvroProperties {}
}

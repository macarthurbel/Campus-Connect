package com.schooldashboard;

import com.schooldashboard.ui.DashboardFactory;
import services.ApplicationBootstrapService;
import services.AuthService;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.util.Optional;

public class App extends Application {

    private Stage primaryStage;
    private final AuthService authService = new AuthService();

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        new ApplicationBootstrapService().initializeBackend();
        primaryStage.setTitle("CampusConnect");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        showLoginScene();
        primaryStage.show();
    }

    private void showLoginScene() {
        StackPane root = new StackPane();
        root.getStyleClass().add("login-root");

        Region decorOne = new Region();
        decorOne.getStyleClass().add("login-blob-one");

        Region decorTwo = new Region();
        decorTwo.getStyleClass().add("login-blob-two");

        StackPane logo = new StackPane();
        logo.getStyleClass().add("login-logo");
        Label logoText = new Label("CC");
        logoText.getStyleClass().add("login-logo-text");
        logo.getChildren().add(logoText);

        Label welcome = new Label("Bienvenue sur CampusConnect");
        welcome.getStyleClass().add("login-title");

        Label subtitle = new Label("Connexion");
        subtitle.getStyleClass().add("login-subtitle");

        ComboBox<String> profile = new ComboBox<>();
        profile.getItems().addAll("Administrateur", "Enseignant", "Etudiant");
        profile.setPromptText("Profil");
        profile.getStyleClass().add("login-field");

        TextField matricule = new TextField();
        matricule.setPromptText("Identifiant");
        matricule.getStyleClass().add("login-field");

        PasswordField password = new PasswordField();
        password.setPromptText("Mot de passe");
        password.getStyleClass().add("login-field");

        Label status = new Label(" ");
        status.getStyleClass().add("login-status");

        Button loginButton = new Button("Se connecter");
        loginButton.getStyleClass().add("login-button");

        Button forgotButton = new Button("Mot de passe oublié ?");
        forgotButton.getStyleClass().add("login-link");
        forgotButton.setOnAction(event -> {
            Optional<String> roleValue = Optional.ofNullable(profile.getValue());
            String identifierValue = matricule.getText() == null ? "" : matricule.getText().trim();

            if (roleValue.isEmpty() || identifierValue.isBlank()) {
                status.setText("Choisissez un profil et saisissez votre identifiant avant de reinitialiser.");
                status.getStyleClass().setAll("login-status", "login-status-error");
                return;
            }

            showPasswordResetDialog(roleValue.get(), identifierValue, status, password);
        });

        loginButton.setOnAction(event -> {
            String selectedProfile = profile.getValue();
            String identifierValue = matricule.getText() == null ? "" : matricule.getText().trim();
            String passwordValue = password.getText() == null ? "" : password.getText().trim();

            if (selectedProfile == null || identifierValue.isEmpty() || passwordValue.isEmpty()) {
                status.setText("Veuillez remplir le profil, l'identifiant et le mot de passe.");
                status.getStyleClass().setAll("login-status", "login-status-error");
                return;
            }

            loginButton.setDisable(true);
            loginButton.setText("Connexion...");
            status.setText("Verification en cours...");
            status.getStyleClass().setAll("login-status", "login-status-info");

            try {
                var account = authService.authenticate(selectedProfile, identifierValue, passwordValue);
                String roleTitle = switch (account.getRole()) {
                    case ADMIN -> "Admin Dashboard";
                    case TEACHER -> "Teacher Dashboard";
                    case STUDENT -> "Student Dashboard";
                };
                showDashboardScene(roleTitle, account.getUsername(), account.getLinkedEntityId());
            } catch (RuntimeException ex) {
                loginButton.setDisable(false);
                loginButton.setText("Se connecter");
                status.setText(ex.getMessage());
                status.getStyleClass().setAll("login-status", "login-status-error");
            }
        });

        profile.valueProperty().addListener((observable, oldValue, newValue) -> {
            matricule.setPromptText(identifierPromptFor(newValue));
        });

        Label helper = new Label("Utilisez l'identifiant et le mot de passe definis par l'administrateur.");
        helper.getStyleClass().add("login-helper");

        VBox card = new VBox(10, logo, welcome, subtitle, profile, buildFieldGroup("Identifiant", matricule), buildFieldGroup("Mot de passe", password), helper, loginButton, forgotButton, status);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(320);
        card.setPadding(new Insets(20, 24, 18, 24));

        VBox brand = new VBox(4);
        brand.setAlignment(Pos.CENTER);

        VBox content = new VBox(16, card);
        content.setAlignment(Pos.CENTER);

        root.getChildren().addAll(decorOne, decorTwo, content);

        Scene loginScene = new Scene(root, 900, 600);
        attachStyles(loginScene);
        primaryStage.setScene(loginScene);
    }

    private void showDashboardScene(String roleTitle, String username, Integer linkedEntityId) {
        Scene dashboardScene = DashboardFactory.createDashboardScene(roleTitle, username, linkedEntityId, this::showLoginScene);
        attachStyles(dashboardScene);
        primaryStage.setScene(dashboardScene);
    }

    private void attachStyles(Scene scene) {
        String stylePath = getClass().getResource("/styles/dashboard.css").toExternalForm();
        scene.getStylesheets().setAll(stylePath);
    }

    private VBox buildFieldGroup(String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.getStyleClass().add("login-label");

        VBox group = new VBox(6, label, field);
        group.setFillWidth(true);
        return group;
    }

    private String identifierPromptFor(String profile) {
        return "Identifiant de connexion";
    }

    private void showPasswordResetDialog(String roleLabel, String username, Label status, PasswordField loginPasswordField) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Gestion du mot de passe");

        Label header = new Label("Reinitialiser l'acces");
        header.getStyleClass().add("login-title");

        Label details = new Label("Compte : " + username + "\nProfil : " + roleLabel);
        details.getStyleClass().add("login-helper");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Nouveau mot de passe");
        newPasswordField.getStyleClass().add("login-field");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirmer le mot de passe");
        confirmPasswordField.getStyleClass().add("login-field");

        Label feedback = new Label(" ");
        feedback.getStyleClass().add("login-status");

        Button cancelButton = new Button("Annuler");
        cancelButton.getStyleClass().add("login-link");
        cancelButton.setOnAction(event -> dialog.close());

        Button saveButton = new Button("Mettre a jour");
        saveButton.getStyleClass().add("login-button");
        saveButton.setOnAction(event -> {
            String newPassword = newPasswordField.getText() == null ? "" : newPasswordField.getText().trim();
            String confirmedPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText().trim();

            if (newPassword.isBlank() || confirmedPassword.isBlank()) {
                feedback.setText("Veuillez saisir et confirmer un nouveau mot de passe.");
                feedback.getStyleClass().setAll("login-status", "login-status-error");
                return;
            }

            if (!newPassword.equals(confirmedPassword)) {
                feedback.setText("Les deux mots de passe ne correspondent pas.");
                feedback.getStyleClass().setAll("login-status", "login-status-error");
                return;
            }

            try {
                boolean updated = authService.resetPassword(roleLabel, username, newPassword);
                if (updated) {
                    feedback.setText("Mot de passe mis a jour avec succes.");
                    feedback.getStyleClass().setAll("login-status", "login-status-success");
                    status.setText("Mot de passe mis a jour. Vous pouvez vous connecter.");
                    status.getStyleClass().setAll("login-status", "login-status-success");
                    loginPasswordField.clear();
                    dialog.close();
                } else {
                    feedback.setText("Impossible de mettre a jour le mot de passe.");
                    feedback.getStyleClass().setAll("login-status", "login-status-error");
                }
            } catch (RuntimeException ex) {
                feedback.setText(ex.getMessage());
                feedback.getStyleClass().setAll("login-status", "login-status-error");
            }
        });

        VBox actions = new VBox(10, saveButton, cancelButton);
        actions.setAlignment(Pos.CENTER);

        VBox content = new VBox(12, header, details, newPasswordField, confirmPasswordField, actions, feedback);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));
        content.setMaxWidth(360);
        content.getStyleClass().add("login-card");

        Scene scene = new Scene(new StackPane(content), 420, 420);
        attachStyles(scene);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

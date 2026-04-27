package com.schooldashboard;

import com.schooldashboard.ui.DashboardFactory;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
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

        ImageView logo = new ImageView(new Image("file:Images/LOGIN.png"));
        logo.setFitWidth(78);
        logo.setFitHeight(78);
        logo.setPreserveRatio(true);
        logo.getStyleClass().add("login-logo");

        Label welcome = new Label("Bienvenue sur CampusConnect");
        welcome.getStyleClass().add("login-title");

        Label subtitle = new Label("Connexion");
        subtitle.getStyleClass().add("login-subtitle");

        ComboBox<String> profile = new ComboBox<>();
        profile.getItems().addAll("Administrateur", "Enseignant", "Etudiant");
        profile.setPromptText("Profil");
        profile.getStyleClass().add("login-field");

        TextField matricule = new TextField();
        matricule.setPromptText("Matricule");
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
            status.setText("Contactez l'administration pour reinitialiser le mot de passe.");
            status.getStyleClass().setAll("login-status", "login-status-info");
        });

        loginButton.setOnAction(event -> {
            String selectedProfile = profile.getValue();
            String matriculeValue = matricule.getText() == null ? "" : matricule.getText().trim();
            String passwordValue = password.getText() == null ? "" : password.getText().trim();

            if (selectedProfile == null || matriculeValue.isEmpty() || passwordValue.isEmpty()) {
                status.setText("Veuillez remplir le profil, le matricule et le mot de passe.");
                status.getStyleClass().setAll("login-status", "login-status-error");
                return;
            }

            loginButton.setDisable(true);
            loginButton.setText("Connexion...");
            status.setText("Verification en cours...");
            status.getStyleClass().setAll("login-status", "login-status-info");
            try {
                loginButton.setDisable(false);
                loginButton.setText("Se connecter");
                status.setText("Connexion reussie.");
                status.getStyleClass().setAll("login-status", "login-status-success");

                String roleTitle = switch (selectedProfile) {
                    case "Administrateur" -> "Admin Dashboard";
                    case "Enseignant" -> "Teacher Dashboard";
                    default -> "Student Dashboard";
                };
                showDashboardScene(roleTitle);
            } catch (RuntimeException ex) {
                loginButton.setDisable(false);
                loginButton.setText("Se connecter");
                status.setText(ex.getMessage());
                status.getStyleClass().setAll("login-status", "login-status-error");
            }
        });

        VBox card = new VBox(10, logo, welcome, subtitle, profile, buildFieldGroup("Matricule", matricule), buildFieldGroup("Mot de passe", password), loginButton, forgotButton, status);
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

    private void showDashboardScene(String roleTitle) {
        Scene dashboardScene = DashboardFactory.createDashboardScene(roleTitle, this::showLoginScene);
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

    public static void main(String[] args) {
        launch(args);
    }
}

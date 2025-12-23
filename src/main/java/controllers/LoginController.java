package controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.User;
import services.UserService;
import util.AlertBox;
import util.Session;
import util.Validator;

public class LoginController {
    private Stage primaryStage;
    private Scene scene;
    private UserService userService;
    
    // UI Components
    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> roleComboBox;
    private Button loginButton;
    private Button registerButton;
    private Label errorLabel;
    
    public LoginController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.userService = new UserService();
        buildUI();
    }
    
    private void buildUI() {
        // Main container with gradient background
        BorderPane mainPane = new BorderPane();
        mainPane.getStyleClass().add("login-background");
        
        // Center container
        StackPane centerContainer = new StackPane();
        centerContainer.setPadding(new Insets(40));
        
        // Login card
        HBox loginCard = new HBox();
        loginCard.getStyleClass().add("login-card");
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(1000);
        loginCard.setMaxHeight(600);
        
        // Left side - Branding
        VBox leftSide = createBrandingSection();
        
        // Right side - Login Form
        VBox rightSide = createLoginForm();
        
        loginCard.getChildren().addAll(leftSide, rightSide);
        centerContainer.getChildren().add(loginCard);
        mainPane.setCenter(centerContainer);
        
        scene = new Scene(mainPane, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
    }
    
    private VBox createBrandingSection() {
        VBox brandingSection = new VBox(30);
        brandingSection.setAlignment(Pos.CENTER);
        brandingSection.setPrefWidth(500);
        brandingSection.setPadding(new Insets(60));
        brandingSection.setStyle("-fx-background-color: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); " +
                                "-fx-background-radius: 20px 0 0 20px;");
        
        // Logo/Icon
        ImageView logoView = new ImageView();
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/pet-icon.png"));
            logoView.setImage(logo);
            logoView.setFitWidth(150);
            logoView.setFitHeight(150);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            // Fallback text logo
            Label textLogo = new Label("🐾");
            textLogo.setFont(Font.font("Arial", 100));
            brandingSection.getChildren().add(textLogo);
        }
        
        // App Name
        Label appName = new Label("NexusFauna");
        appName.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        appName.setTextFill(Color.WHITE);
        
        // Tagline
        Label tagline = new Label("Pet Adoption System");
        tagline.setFont(Font.font("Arial", 20));
        tagline.setTextFill(Color.rgb(255, 255, 255, 0.9));
        
        // Features
        VBox features = new VBox(15);
        features.setAlignment(Pos.CENTER_LEFT);
        
        String[] featureItems = {
            "✓ Find loving homes for pets",
            "✓ Easy adoption process",
            "✓ Track applications",
            "✓ Secure and reliable",
            "✓ 24/7 Support"
        };
        
        for (String feature : featureItems) {
            Label featureLabel = new Label(feature);
            featureLabel.setFont(Font.font("Arial", 14));
            featureLabel.setTextFill(Color.WHITE);
            features.getChildren().add(featureLabel);
        }
        
        brandingSection.getChildren().addAll(logoView, appName, tagline, features);
        return brandingSection;
    }
    
    private VBox createLoginForm() {
        VBox formSection = new VBox(30);
        formSection.setAlignment(Pos.CENTER);
        formSection.setPrefWidth(500);
        formSection.setPadding(new Insets(60, 50, 60, 50));
        
        // Title
        Label title = new Label("Welcome Back");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.web("#1e293b"));
        
        Label subtitle = new Label("Sign in to your account");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setTextFill(Color.web("#64748b"));
        
        // Form
        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER);
        
        // Username
        VBox usernameBox = new VBox(8);
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(45);
        usernameBox.getChildren().addAll(usernameLabel, usernameField);
        
        // Password
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(45);
        passwordBox.getChildren().addAll(passwordLabel, passwordField);
        
        // Role
        VBox roleBox = new VBox(8);
        Label roleLabel = new Label("Login As");
        roleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Administrator", "Adopter");
        roleComboBox.setValue("Adopter");
        roleComboBox.setPrefHeight(45);
        roleBox.getChildren().addAll(roleLabel, roleComboBox);
        
        // Error label
        errorLabel = new Label();
        errorLabel.getStyleClass().add("label-error");
        errorLabel.setVisible(false);
        
        // Login button
        loginButton = new Button("Sign In");
        loginButton.getStyleClass().add("button-primary");
        loginButton.setPrefWidth(300);
        loginButton.setPrefHeight(45);
        loginButton.setOnAction(e -> handleLogin());
        
        // Register link
        HBox registerBox = new HBox(5);
        registerBox.setAlignment(Pos.CENTER);
        Label registerLabel = new Label("Don't have an account?");
        registerButton = new Button("Register Here");
        registerButton.getStyleClass().add("button-outline");
        registerButton.setOnAction(e -> showRegistrationDialog());
        registerBox.getChildren().addAll(registerLabel, registerButton);
        
        form.getChildren().addAll(usernameBox, passwordBox, roleBox, errorLabel, loginButton, registerBox);
        formSection.getChildren().addAll(title, subtitle, form);
        
        return formSection;
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();
        
        // Validation
        if (!Validator.isNotEmpty(usernameField) || !Validator.isNotEmpty(passwordField)) {
            showError("Please fill in all fields");
            return;
        }
        
        User user = userService.validateLogin(username, password, role);
        
        if (user != null) {
            // Set session
            Session.setCurrentUser(user);
            Session.setUserRole(role);
            
            // Navigate to appropriate dashboard
            if (role.equals("Administrator")) {
                AdminDashboardController adminController = new AdminDashboardController(primaryStage);
                primaryStage.setScene(adminController.getScene());
            } else {
                AdopterDashboardController adopterController = new AdopterDashboardController(primaryStage);
                primaryStage.setScene(adopterController.getScene());
            }
            primaryStage.setMaximized(true);
        } else {
            showError("Invalid username, password, or role");
        }
    }
    
    private void showRegistrationDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Register as Adopter");
        
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setAlignment(Pos.CENTER);
        
        Label title = new Label("Create New Account");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Form
        VBox form = new VBox(15);
        
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        fullNameField.setPrefHeight(40);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.setPrefHeight(40);
        
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone (optional)");
        phoneField.setPrefHeight(40);
        
        TextField addressField = new TextField();
        addressField.setPromptText("Address (optional)");
        addressField.setPrefHeight(40);
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefHeight(40);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password (min 6 characters)");
        passwordField.setPrefHeight(40);
        
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        confirmPasswordField.setPrefHeight(40);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button registerBtn = new Button("Register");
        registerBtn.getStyleClass().add("button-success");
        registerBtn.setOnAction(e -> {
            String error = Validator.validateRegistration(
                fullNameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                usernameField.getText(),
                passwordField.getText(),
                confirmPasswordField.getText()
            );
            
            if (error != null) {
                AlertBox.showError("Validation Error", error);
                return;
            }
            
            // Check if username exists
            if (userService.checkUsernameExists(usernameField.getText())) {
                AlertBox.showError("Registration Error", "Username already exists");
                return;
            }
            
            User newUser = new User(
                usernameField.getText(),
                passwordField.getText(),
                "Adopter",
                fullNameField.getText(),
                emailField.getText(),
                phoneField.getText(),
                addressField.getText()
            );
            
            boolean success = userService.registerUser(newUser);
            if (success) {
                AlertBox.showInfo("Success", "Registration successful! You can now login.");
                dialog.close();
            } else {
                AlertBox.showError("Error", "Registration failed. Please try again.");
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button-secondary");
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(registerBtn, cancelBtn);
        
        form.getChildren().addAll(
            fullNameField, emailField, phoneField, addressField,
            usernameField, passwordField, confirmPasswordField,
            buttonBox
        );
        
        dialogContent.getChildren().addAll(title, form);
        
        Scene dialogScene = new Scene(dialogContent, 400, 550);
        dialogScene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    public Scene getScene() {
        return scene;
    }
}
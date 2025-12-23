package app;

import controllers.LoginController;
import dao.DatabaseConnection;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Initialize database connection
        try {
            DatabaseConnection.getConnection();
            System.out.println("Database connected successfully");
        } catch (Exception e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            // Continue with application (for demo purposes)
        }
        
        // Create login controller
        LoginController loginController = new LoginController(primaryStage);
        Scene loginScene = loginController.getScene();
        
        // Configure primary stage
        primaryStage.setTitle("NexusFauna - Pet Adoption System");
        primaryStage.setScene(loginScene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        
        // Set application icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/pet-icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Icon not found, using default");
        }
        
        primaryStage.show();
        
        // Handle application close
        primaryStage.setOnCloseRequest(e -> {
            DatabaseConnection.closeConnection();
            System.exit(0);
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
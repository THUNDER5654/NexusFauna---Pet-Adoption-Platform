package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.DBConnection;
import util.AlertBox;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1️⃣ Initialize database
            if (!DBConnection.connect()) {
                AlertBox.showError("Database Error", "Unable to connect to the database.");
                System.exit(1);
            }

            // 2️⃣ Load initial UI (Login)
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Login.fxml")
            );

            Scene scene = new Scene(loader.load());

            // 3️⃣ Apply CSS
            scene.getStylesheets().add(
                    getClass().getResource("/styles/Styles.css").toExternalForm()
            );

            // 4️⃣ Configure stage
            primaryStage.setTitle("Pet Adoption System");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

            // 5️⃣ Close DB on exit
            primaryStage.setOnCloseRequest(event -> DBConnection.disconnect());

        } catch (Exception e) {
            e.printStackTrace();
            AlertBox.showError("Startup Error", "Application failed to start.");
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

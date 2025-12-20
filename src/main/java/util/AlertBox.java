package util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

public class AlertBox {
    
    public static void showInfo(String title, String message) {
        showAlert(AlertType.INFORMATION, title, message);
    }
    
    public static void showError(String title, String message) {
        showAlert(AlertType.ERROR, title, message);
    }
    
    public static void showWarning(String title, String message) {
        showAlert(AlertType.WARNING, title, message);
    }
    
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert
        try {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                AlertBox.class.getResource("/styles/Styles.css").toExternalForm()
            );
        } catch (Exception e) {
            // Styles not found, continue without
        }
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    private static void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert
        try {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                AlertBox.class.getResource("/styles/Styles.css").toExternalForm()
            );
        } catch (Exception e) {
            // Styles not found, continue without
        }
        
        alert.showAndWait();
    }
}
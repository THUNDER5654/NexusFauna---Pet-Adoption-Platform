package util;

import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import java.util.regex.Pattern;

public class Validator {
    
    public static boolean isNotEmpty(TextField field) {
        return field.getText() != null && !field.getText().trim().isEmpty();
    }
    
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return true; // Phone is optional
        String phoneRegex = "^[0-9]{10}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        return pattern.matcher(phone).matches();
    }
    
    public static boolean isValidAge(String ageStr) {
        try {
            int age = Integer.parseInt(ageStr);
            return age >= 0 && age <= 50;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    public static boolean passwordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }
    
    public static boolean isSelected(ComboBox<?> comboBox) {
        return comboBox.getValue() != null;
    }
    
    public static String validateRegistration(String fullName, String email, String phone,
                                            String username, String password, String confirmPassword) {
        if (!isNotEmpty(fullName)) {
            return "Full name is required";
        }
        
        if (!isNotEmpty(email)) {
            return "Email is required";
        }
        
        if (!isValidEmail(email)) {
            return "Invalid email format";
        }
        
        if (!isValidPhone(phone)) {
            return "Phone number must be 10 digits";
        }
        
        if (!isNotEmpty(username)) {
            return "Username is required";
        }
        
        if (!isValidPassword(password)) {
            return "Password must be at least 6 characters";
        }
        
        if (!passwordsMatch(password, confirmPassword)) {
            return "Passwords do not match";
        }
        
        return null; // No errors
    }
    
    public static String validatePet(String name, String type, String ageStr, 
                                    String gender, String healthStatus) {
        if (!isNotEmpty(name)) {
            return "Pet name is required";
        }
        
        if (!isNotEmpty(type)) {
            return "Pet type is required";
        }
        
        if (!isValidAge(ageStr)) {
            return "Age must be a valid number between 0 and 50";
        }
        
        if (!isNotEmpty(gender)) {
            return "Gender is required";
        }
        
        if (!isNotEmpty(healthStatus)) {
            return "Health status is required";
        }
        
        return null; // No errors
    }
}
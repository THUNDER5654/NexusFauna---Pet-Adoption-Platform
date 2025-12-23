package util;

import models.User;

public class Session {
    private static User currentUser;
    private static String userRole;
    
    public static void setCurrentUser(User user) {
        currentUser = user;
        if (user != null) {
            userRole = user.getRole();
        }
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static void setUserRole(String role) {
        userRole = role;
    }
    
    public static String getUserRole() {
        return userRole;
    }
    
    public static boolean isAdmin() {
        return "Administrator".equals(userRole);
    }
    
    public static boolean isAdopter() {
        return "Adopter".equals(userRole);
    }
    
    public static void clear() {
        currentUser = null;
        userRole = null;
    }
    
    public static int getUserId() {
        return currentUser != null ? currentUser.getId() : 0;
    }
    
    public static String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "";
    }
    
    public static String getUserFullName() {
        return currentUser != null ? currentUser.getFullName() : "";
    }
    
    public static String getUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "";
    }
    
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
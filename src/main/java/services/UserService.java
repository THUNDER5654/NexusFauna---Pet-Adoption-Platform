package services;

import dao.UserDAO;
import models.User;
import java.sql.SQLException;

public class UserService {
    private UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
    }
    
    public User validateLogin(String username, String password, String role) {
        try {
            return userDAO.validateLogin(username, password, role);
        } catch (SQLException e) {
            System.err.println("Error validating login: " + e.getMessage());
            return null;
        }
    }
    
    public boolean registerUser(User user) {
        try {
            // Check if username already exists
            if (userDAO.checkUsernameExists(user.getUsername())) {
                return false;
            }
            return userDAO.registerUser(user);
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }
    
    public User getUserById(int id) {
        try {
            return userDAO.getUserById(id);
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            return null;
        }
    }
    
    public boolean checkUsernameExists(String username) {
        try {
            return userDAO.checkUsernameExists(username);
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
            return false;
        }
    }
}
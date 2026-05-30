package com.lostandfound.controllers;

import com.lostandfound.dao.UserDAO;
import com.lostandfound.main.Main;
import com.lostandfound.main.SessionManager;
import com.lostandfound.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error", "Please enter both email and password.");
            return;
        }

        User user = userDAO.login(email, password);
        if (user != null) {
            SessionManager.setCurrentUser(user);
            Main.setRoot("Dashboard", "Lost & Found - Dashboard", 1024, 768);
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
        }
    }

    @FXML
    private void handleGoToRegister() {
        Main.setRoot("Register", "Lost & Found - Register", 800, 600);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

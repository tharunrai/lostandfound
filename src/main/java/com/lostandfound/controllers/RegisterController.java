package com.lostandfound.controllers;

import com.lostandfound.dao.UserDAO;
import com.lostandfound.main.Main;
import com.lostandfound.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Form Error", "Please fill in all required fields.");
            return;
        }

        if (!password.equals(confirm)) {
            showAlert(Alert.AlertType.ERROR, "Form Error", "Passwords do not match.");
            return;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(password); // Will be hashed in DAO

        if (userDAO.register(user)) {
            showAlert(Alert.AlertType.INFORMATION, "Registration Successful", "You can now login with your credentials.");
            Main.setRoot("Login", "Lost & Found - Login", 800, 600);
        } else {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "Email may already exist or database error.");
        }
    }

    @FXML
    private void handleGoToLogin() {
        Main.setRoot("Login", "Lost & Found - Login", 800, 600);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

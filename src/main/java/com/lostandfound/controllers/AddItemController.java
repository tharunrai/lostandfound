package com.lostandfound.controllers;

import com.lostandfound.dao.ItemDAO;
import com.lostandfound.main.Main;
import com.lostandfound.main.SessionManager;
import com.lostandfound.models.Item;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Date;
import java.time.LocalDate;

public class AddItemController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<com.lostandfound.models.Category> categoryBox;
    @FXML private ComboBox<com.lostandfound.models.Location> locationBox;
    @FXML private RadioButton lostRadio;
    @FXML private RadioButton foundRadio;
    @FXML private Label imageNameLabel;

    private ItemDAO itemDAO = new ItemDAO();
    private java.io.File selectedImageFile;

    @FXML
    public void initialize() {
        categoryBox.setItems(FXCollections.observableArrayList(itemDAO.getCategories()));
        locationBox.setItems(FXCollections.observableArrayList(itemDAO.getLocations()));
        
        ToggleGroup group = new ToggleGroup();
        lostRadio.setToggleGroup(group);
        foundRadio.setToggleGroup(group);
        lostRadio.setSelected(true);
    }

    @FXML
    private void handleUploadImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Item Image");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        java.io.File file = fileChooser.showOpenDialog(titleField.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imageNameLabel.setText(file.getName());
        }
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText();
        String description = descriptionArea.getText();
        com.lostandfound.models.Category selectedCategory = categoryBox.getSelectionModel().getSelectedItem();
        com.lostandfound.models.Location selectedLocation = locationBox.getSelectionModel().getSelectedItem();
        String status = lostRadio.isSelected() ? "lost" : "found";

        if (title.isEmpty() || selectedCategory == null || selectedLocation == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all required fields.");
            return;
        }

        int categoryId = selectedCategory.getCategoryId();
        int locationId = selectedLocation.getLocationId();

        String imagePath = null;
        if (selectedImageFile != null) {
            try {
                java.io.File uploadDir = new java.io.File("uploads");
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                
                String extension = "";
                String fileName = selectedImageFile.getName();
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    extension = fileName.substring(dotIndex);
                }
                String newFileName = System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().substring(0, 8) + extension;
                java.io.File destFile = new java.io.File(uploadDir, newFileName);
                
                java.nio.file.Files.copy(
                    selectedImageFile.toPath(), 
                    destFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                
                imagePath = destFile.getPath();
            } catch (java.io.IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Upload Error", "Failed to save selected image file.");
                return;
            }
        }

        Item item = new Item();
        item.setTitle(title);
        item.setDescription(description);
        item.setCategoryId(categoryId);
        item.setLocationId(locationId);
        item.setStatus(status);
        item.setReportedDate(Date.valueOf(LocalDate.now()));
        item.setUserId(SessionManager.getCurrentUser().getUserId());
        item.setImagePath(imagePath);

        if (itemDAO.addItem(item)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Item reported successfully!");
            Main.setRoot("Dashboard", "Lost & Found - Dashboard", 1024, 768);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to report item.");
        }
    }

    @FXML
    private void handleCancel() {
        Main.setRoot("Dashboard", "Lost & Found - Dashboard", 1024, 768);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

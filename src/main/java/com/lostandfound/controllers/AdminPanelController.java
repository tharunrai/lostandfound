package com.lostandfound.controllers;

import com.lostandfound.dao.ClaimDAO;
import com.lostandfound.dao.ItemDAO;
import com.lostandfound.main.Main;
import com.lostandfound.models.Claim;
import com.lostandfound.models.Item;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Date;
import java.util.List;

public class AdminPanelController {

    // ── Claims tab ─────────────────────────────────────────────────
    @FXML private TableView<Claim> claimsTable;
    @FXML private TableColumn<Claim, String> colItemTitle;
    @FXML private TableColumn<Claim, String> colClaimer;
    @FXML private TableColumn<Claim, String> colStatus;
    @FXML private TableColumn<Claim, Date>   colDate;

    // ── Items tab ──────────────────────────────────────────────────
    @FXML private TableView<Item>            itemsTable;
    @FXML private TableColumn<Item, String>  colItemsTitle;
    @FXML private TableColumn<Item, String>  colItemsCategory;
    @FXML private TableColumn<Item, String>  colItemsLocation;
    @FXML private TableColumn<Item, String>  colItemsStatus;
    @FXML private TableColumn<Item, Date>    colItemsDate;
    @FXML private TableColumn<Item, String>  colItemsClaimers;
    @FXML private ComboBox<String>           itemStatusFilter;

    private ClaimDAO claimDAO = new ClaimDAO();
    private ItemDAO  itemDAO  = new ItemDAO();

    private ObservableList<Claim> claimList = FXCollections.observableArrayList();
    private ObservableList<Item>  itemList  = FXCollections.observableArrayList();

    // ── Init ───────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Claims tab columns
        colItemTitle.setCellValueFactory(new PropertyValueFactory<>("itemTitle"));
        colClaimer  .setCellValueFactory(new PropertyValueFactory<>("claimerName"));
        colStatus   .setCellValueFactory(new PropertyValueFactory<>("claimStatus"));
        colDate     .setCellValueFactory(new PropertyValueFactory<>("claimDate"));
        loadClaims();

        // Items tab columns
        colItemsTitle   .setCellValueFactory(new PropertyValueFactory<>("title"));
        colItemsCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colItemsLocation.setCellValueFactory(new PropertyValueFactory<>("locationName"));
        colItemsStatus  .setCellValueFactory(new PropertyValueFactory<>("status"));
        colItemsDate    .setCellValueFactory(new PropertyValueFactory<>("reportedDate"));
        colItemsClaimers.setCellValueFactory(new PropertyValueFactory<>("claimerNames"));

        itemStatusFilter.setItems(FXCollections.observableArrayList("All", "lost", "found", "claimed"));
        itemStatusFilter.setValue("All");

        loadAllItems();
    }

    // ── Claims tab handlers ────────────────────────────────────────
    private void loadClaims() {
        claimList.setAll(claimDAO.getAllClaims());
        claimsTable.setItems(claimList);
    }

    @FXML
    private void handleApprove() {
        Claim selected = claimsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "Warning", "Please select a claim to approve."); return; }

        if ("approved".equalsIgnoreCase(selected.getClaimStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Already Approved", "This claim is already approved.");
            return;
        }
        if (claimDAO.updateClaimStatus(selected.getClaimId(), "approved")) {
            itemDAO.updateStatus(selected.getItemId(), "claimed");
            showAlert(Alert.AlertType.INFORMATION, "Success", "Claim approved — item marked as claimed.");
            loadClaims();
            loadAllItems();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to approve claim.");
        }
    }

    @FXML
    private void handleReject() {
        Claim selected = claimsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "Warning", "Please select a claim to reject."); return; }

        if (claimDAO.updateClaimStatus(selected.getClaimId(), "rejected")) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Claim rejected.");
            loadClaims();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject claim.");
        }
    }

    // ── Items tab handlers ─────────────────────────────────────────
    private void loadAllItems() {
        itemList.setAll(itemDAO.getAllItemsForAdmin());
        itemsTable.setItems(itemList);
    }

    @FXML
    private void handleItemFilter() {
        String filter = itemStatusFilter.getValue();
        List<Item> items;
        if (filter == null || filter.equals("All")) {
            items = itemDAO.getAllItemsForAdmin();
        } else {
            items = itemDAO.getItemsWithClaimersByStatus(filter);
        }
        itemList.setAll(items);
        itemsTable.setItems(itemList);
    }

    @FXML
    private void handleMarkAsFound() {
        Item selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "Warning", "Please select an item from the Items tab."); return; }
        if ("found".equalsIgnoreCase(selected.getStatus())) { showAlert(Alert.AlertType.INFORMATION, "No Change", "Item is already marked as Found."); return; }

        if (itemDAO.updateStatus(selected.getItemId(), "found")) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "\"" + selected.getTitle() + "\" is now marked as Found.");
            loadAllItems();
            loadClaims();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update item status.");
        }
    }

    @FXML
    private void handleMarkAsLost() {
        Item selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "Warning", "Please select an item from the Items tab."); return; }
        if ("lost".equalsIgnoreCase(selected.getStatus())) { showAlert(Alert.AlertType.INFORMATION, "No Change", "Item is already marked as Lost."); return; }

        if (itemDAO.updateStatus(selected.getItemId(), "lost")) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "\"" + selected.getTitle() + "\" is now marked as Lost.");
            loadAllItems();
            loadClaims();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update item status.");
        }
    }

    @FXML
    private void handleMarkAsClaimed() {
        Item selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert(Alert.AlertType.WARNING, "Warning", "Please select an item from the Items tab."); return; }
        if ("claimed".equalsIgnoreCase(selected.getStatus())) { showAlert(Alert.AlertType.INFORMATION, "No Change", "Item is already marked as Claimed."); return; }

        if (itemDAO.updateStatus(selected.getItemId(), "claimed")) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "\"" + selected.getTitle() + "\" is now marked as Claimed.");
            loadAllItems();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update item status.");
        }
    }

    // ── Shared ─────────────────────────────────────────────────────
    @FXML
    private void handleBack() {
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

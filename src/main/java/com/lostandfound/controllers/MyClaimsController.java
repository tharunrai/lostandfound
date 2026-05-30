package com.lostandfound.controllers;

import com.lostandfound.dao.ClaimDAO;
import com.lostandfound.main.Main;
import com.lostandfound.main.SessionManager;
import com.lostandfound.models.Claim;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Date;
import java.util.List;

public class MyClaimsController {

    @FXML private TableView<Claim> claimsTable;
    @FXML private TableColumn<Claim, String> colItemTitle;
    @FXML private TableColumn<Claim, String> colStatus;
    @FXML private TableColumn<Claim, Date> colDate;

    private ClaimDAO claimDAO = new ClaimDAO();
    private ObservableList<Claim> claimList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colItemTitle.setCellValueFactory(new PropertyValueFactory<>("itemTitle"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("claimStatus"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("claimDate"));

        loadClaims();
    }

    private void loadClaims() {
        int userId = SessionManager.getCurrentUser().getUserId();
        List<Claim> claims = claimDAO.getClaimsByUser(userId);
        claimList.setAll(claims);
        claimsTable.setItems(claimList);
    }

    @FXML
    private void handleBack() {
        Main.setRoot("Dashboard", "Lost & Found - Dashboard", 1024, 768);
    }
}

package com.lostandfound.controllers;

import com.lostandfound.dao.ClaimDAO;
import com.lostandfound.dao.MatchReportDAO;
import com.lostandfound.main.Main;
import com.lostandfound.main.SessionManager;
import com.lostandfound.models.Claim;
import com.lostandfound.models.Item;
import com.lostandfound.models.MatchReport;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ItemDetailController {

    @FXML private Text titleText;
    @FXML private Label categoryLabel;
    @FXML private Label locationLabel;
    @FXML private Label statusLabel;
    @FXML private Label reporterLabel;
    @FXML private Label dateLabel;
    @FXML private Text descriptionText;
    @FXML private Button claimButton;
    @FXML private Button matchButton;
    @FXML private javafx.scene.layout.VBox imageContainer;
    @FXML private javafx.scene.image.ImageView itemImageView;
    @FXML private VBox matchReportsBox;

    private Item currentItem;
    private ClaimDAO claimDAO = new ClaimDAO();
    private MatchReportDAO matchReportDAO = new MatchReportDAO();

    @FXML
    public void initialize() {
        currentItem = SessionManager.getCurrentSelectedItem();
        if (currentItem == null) return;

        titleText.setText(currentItem.getTitle());
        categoryLabel.setText("Category: " + currentItem.getCategoryName());
        locationLabel.setText("Location: " + currentItem.getLocationName());
        statusLabel.setText("Status: " + currentItem.getStatus().toUpperCase());
        reporterLabel.setText("Reported by: " + currentItem.getReporterName());
        dateLabel.setText("Date: " + currentItem.getReportedDate().toString());
        descriptionText.setText(currentItem.getDescription());

        // Handle optional image display
        if (currentItem.getImagePath() != null && !currentItem.getImagePath().trim().isEmpty()) {
            java.io.File imgFile = new java.io.File(currentItem.getImagePath());
            if (imgFile.exists()) {
                try {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(imgFile.toURI().toString());
                    itemImageView.setImage(image);
                    imageContainer.setVisible(true);
                    imageContainer.setManaged(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        int currentUserId = SessionManager.getCurrentUser() != null
                ? SessionManager.getCurrentUser().getUserId() : -1;
        boolean isAdmin = SessionManager.getCurrentUser() != null
                && SessionManager.getCurrentUser().isAdmin();
        boolean isOwner = (currentItem.getUserId() == currentUserId);
        String status = currentItem.getStatus();

        // ── Claim button — only for 'found' items, non-admin regular users ──
        if (!"found".equalsIgnoreCase(status) || isAdmin) {
            claimButton.setVisible(false);
            claimButton.setManaged(false);
        } else if (claimDAO.hasUserClaimedItem(currentUserId, currentItem.getItemId())) {
            claimButton.setText("Already Claimed");
            claimButton.setDisable(true);
        }

        // ── "Found a Match?" — shown for ALL lost items to everyone ──
        if ("lost".equalsIgnoreCase(status)) {
            matchButton.setVisible(true);
            matchButton.setManaged(true);

            if (isOwner) {
                // Owner sees the button but it's informational/disabled
                int count = matchReportDAO.getMatchReportCount(currentItem.getItemId());
                matchButton.setText("🔍 " + count + " Match Report" + (count != 1 ? "s" : ""));
                matchButton.setDisable(true);
                matchButton.getStyleClass().clear();
                matchButton.getStyleClass().add("match-button-done");
            } else if (isAdmin) {
                // Admin sees count info only
                int count = matchReportDAO.getMatchReportCount(currentItem.getItemId());
                matchButton.setText("🔍 " + count + " Match Report" + (count != 1 ? "s" : ""));
                matchButton.setDisable(true);
                matchButton.getStyleClass().clear();
                matchButton.getStyleClass().add("match-button-done");
            } else {
                // Other users can submit a match report
                boolean alreadyReported = matchReportDAO.hasUserReportedMatch(
                        currentUserId, currentItem.getItemId());
                if (alreadyReported) {
                    matchButton.setText("✔ Match Already Reported");
                    matchButton.setDisable(true);
                    matchButton.getStyleClass().clear();
                    matchButton.getStyleClass().add("match-button-done");
                }
                // else: default state — clickable teal button
            }
        } else {
            matchButton.setVisible(false);
            matchButton.setManaged(false);
        }

        // ── Load existing match reports panel ──
        loadMatchReports();
    }

    /** Loads and renders the match-reports section below the detail cards. */
    private void loadMatchReports() {
        if (matchReportsBox == null) return;
        matchReportsBox.getChildren().clear();

        if (!"lost".equalsIgnoreCase(currentItem.getStatus())) {
            matchReportsBox.setVisible(false);
            matchReportsBox.setManaged(false);
            return;
        }

        List<MatchReport> reports = matchReportDAO.getMatchReportsByItem(currentItem.getItemId());

        if (reports.isEmpty()) {
            matchReportsBox.setVisible(false);
            matchReportsBox.setManaged(false);
            return;
        }

        matchReportsBox.setVisible(true);
        matchReportsBox.setManaged(true);

        Label header = new Label("🔎  People who reported finding this item  (" + reports.size() + ")");
        header.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #A1A1AA; " +
                "-fx-font-family: 'Segoe UI Semibold', Arial, sans-serif;");
        matchReportsBox.getChildren().add(header);

        for (MatchReport r : reports) {
            VBox card = new VBox(4);
            card.setPadding(new Insets(10, 14, 10, 14));
            card.setStyle("-fx-background-color: #F0FDF4; -fx-background-radius: 8px; " +
                    "-fx-border-color: #BBF7D0; -fx-border-width: 1px; -fx-border-radius: 8px;");

            Label who = new Label("👤  " + r.getReporterName() + "   ·   " + r.getReportDate().toString());
            who.setStyle("-fx-font-size: 12px; -fx-text-fill: #065F46; -fx-font-weight: bold;");

            Label noteLabel = new Label(
                    r.getNote() != null && !r.getNote().isBlank()
                            ? r.getNote()
                            : "Reported a potential match for this item.");
            noteLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151; -fx-wrap-text: true;");
            noteLabel.setWrapText(true);
            noteLabel.setMaxWidth(480);

            card.getChildren().addAll(who, noteLabel);
            matchReportsBox.getChildren().add(card);
        }
    }

    @FXML
    private void handleFoundMatch() {
        if (currentItem == null) return;

        int userId = SessionManager.getCurrentUser().getUserId();

        if (matchReportDAO.hasUserReportedMatch(userId, currentItem.getItemId())) {
            showAlert(Alert.AlertType.WARNING, "Already Reported",
                    "You have already submitted a match report for this item.");
            return;
        }

        // Confirm dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Report Found Match");
        confirm.setHeaderText("Report a match for: \"" + currentItem.getTitle() + "\"");
        confirm.setContentText(
                "You are reporting that you have seen or found this lost item.\n\n" +
                "The item's reporter will be notified. Do you want to proceed?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            MatchReport report = new MatchReport();
            report.setItemId(currentItem.getItemId());
            report.setReporterId(userId);
            report.setReportDate(Date.valueOf(LocalDate.now()));

            if (matchReportDAO.addMatchReport(report)) {
                showAlert(Alert.AlertType.INFORMATION, "Report Submitted",
                        "Your match report has been submitted! The owner of this item will be informed.");
                // Refresh button state
                matchButton.setText("✔ Match Already Reported");
                matchButton.setDisable(true);
                matchButton.getStyleClass().clear();
                matchButton.getStyleClass().add("match-button-done");
                loadMatchReports();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to submit match report. You may have already reported this item.");
            }
        }
    }

    @FXML
    private void handleClaim() {
        if (currentItem == null) return;

        int userId = SessionManager.getCurrentUser().getUserId();
        if (claimDAO.hasUserClaimedItem(userId, currentItem.getItemId())) {
            showAlert(Alert.AlertType.WARNING, "Already Claimed",
                    "You have already submitted a claim for this item.");
            return;
        }

        Claim claim = new Claim();
        claim.setItemId(currentItem.getItemId());
        claim.setClaimerId(userId);
        claim.setClaimStatus("pending");
        claim.setClaimDate(Date.valueOf(LocalDate.now()));

        if (claimDAO.addClaim(claim)) {
            showAlert(Alert.AlertType.INFORMATION, "Claim Submitted",
                    "Your claim has been submitted successfully and is pending admin approval.");
            Main.setRoot("Dashboard", "Lost & Found - Dashboard", 1024, 768);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to submit claim. You might have already claimed this item.");
        }
    }

    @FXML
    private void handleBack() {
        SessionManager.setCurrentSelectedItem(null);
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

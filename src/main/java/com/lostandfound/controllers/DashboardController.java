package com.lostandfound.controllers;

import com.lostandfound.dao.ItemDAO;
import com.lostandfound.dao.MatchReportDAO;
import com.lostandfound.main.Main;
import com.lostandfound.main.SessionManager;
import com.lostandfound.models.Item;
import com.lostandfound.models.MatchReport;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class DashboardController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Button toggleViewBtn;
    
    // Lost table
    @FXML private TableView<Item> lostTable;
    @FXML private TableColumn<Item, String> colLostTitle;
    @FXML private TableColumn<Item, String> colLostCategory;
    @FXML private TableColumn<Item, String> colLostLocation;
    @FXML private TableColumn<Item, String> colLostStatus;
    @FXML private TableColumn<Item, Date> colLostDate;

    // Found table
    @FXML private TableView<Item> foundTable;
    @FXML private TableColumn<Item, String> colFoundTitle;
    @FXML private TableColumn<Item, String> colFoundCategory;
    @FXML private TableColumn<Item, String> colFoundLocation;
    @FXML private TableColumn<Item, String> colFoundStatus;
    @FXML private TableColumn<Item, Date> colFoundDate;
    @FXML private TableColumn<Item, String> colFoundClaimers;

    // Card View components
    @FXML private ScrollPane lostCardsScroll;
    @FXML private FlowPane lostCardsPane;
    @FXML private ScrollPane foundCardsScroll;
    @FXML private FlowPane foundCardsPane;
    
    @FXML private Button adminPanelBtn;

    private ItemDAO itemDAO = new ItemDAO();
    private MatchReportDAO matchReportDAO = new MatchReportDAO();
    private ObservableList<Item> lostList = FXCollections.observableArrayList();
    private ObservableList<Item> foundList = FXCollections.observableArrayList();
    private boolean isTableView = false;

    @FXML
    public void initialize() {
        // Init filters based on seed categories
        categoryFilter.setItems(FXCollections.observableArrayList("All", "Electronics", "Bags", "Keys", "Clothing", "Documents", "Other"));
        categoryFilter.setValue("All");
        
        // Init columns for Lost Table
        colLostTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colLostCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colLostLocation.setCellValueFactory(new PropertyValueFactory<>("locationName"));
        colLostStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colLostDate.setCellValueFactory(new PropertyValueFactory<>("reportedDate"));

        // Init columns for Found Table
        colFoundTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colFoundCategory.setCellValueFactory(new PropertyValueFactory<>("categoryName"));
        colFoundLocation.setCellValueFactory(new PropertyValueFactory<>("locationName"));
        colFoundStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colFoundDate.setCellValueFactory(new PropertyValueFactory<>("reportedDate"));
        colFoundClaimers.setCellValueFactory(new PropertyValueFactory<>("claimerNames"));

        // Hide admin button if not admin
        if (SessionManager.getCurrentUser() != null && !SessionManager.getCurrentUser().isAdmin()) {
            adminPanelBtn.setVisible(false);
            adminPanelBtn.setManaged(false);
        }

        // Add double click listeners on both tables
        lostTable.setRowFactory(tv -> createRowFactory());
        foundTable.setRowFactory(tv -> createRowFactory());

        loadItems();

        // Default to card view — button label reflects current state
        toggleViewBtn.setText("📋  Table View");
    }

    private TableRow<Item> createRowFactory() {
        TableRow<Item> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                Item rowData = row.getItem();
                SessionManager.setCurrentSelectedItem(rowData);
                Main.setRoot("ItemDetail", "Lost & Found - Item Details", 800, 600);
            }
        });
        return row;
    }

    private void loadItems() {
        List<Item> items = itemDAO.getAllItems();
        populateTables(items);
    }

    private void populateTables(List<Item> items) {
        lostList.clear();
        foundList.clear();
        for (Item item : items) {
            if ("lost".equalsIgnoreCase(item.getStatus())) {
                lostList.add(item);
            }
        }
        // Found items always loaded with claimer names from dedicated query
        foundList.setAll(itemDAO.getItemsWithClaimersByStatus("found"));

        lostTable.setItems(lostList);
        foundTable.setItems(foundList);
        
        if (!isTableView) {
            renderCards();
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        String category = categoryFilter.getValue();

        List<Item> items = itemDAO.searchItems(keyword, category, "All");
        populateTables(items);
    }

    @FXML
    private void handleToggleView() {
        isTableView = !isTableView;
        
        lostTable.setVisible(isTableView);
        lostTable.setManaged(isTableView);
        lostCardsScroll.setVisible(!isTableView);
        lostCardsScroll.setManaged(!isTableView);

        foundTable.setVisible(isTableView);
        foundTable.setManaged(isTableView);
        foundCardsScroll.setVisible(!isTableView);
        foundCardsScroll.setManaged(!isTableView);

        if (isTableView) {
            toggleViewBtn.setText("🎴  Cards View");
        } else {
            toggleViewBtn.setText("📋  Table View");
            renderCards();
        }
    }

    private void renderCards() {
        lostCardsPane.getChildren().clear();
        foundCardsPane.getChildren().clear();

        for (Item item : lostList) {
            lostCardsPane.getChildren().add(createCard(item));
        }

        for (Item item : foundList) {
            foundCardsPane.getChildren().add(createCard(item));
        }
    }

    private VBox createCard(Item item) {
        VBox card = new VBox(10);
        card.getStyleClass().add("item-card");
        card.setPrefWidth(230);
        card.setPadding(new Insets(14));

        // ── Item image: cover-crop into fixed 202×148 container ──────
        String imagePath = item.getImagePath();
        if (imagePath != null && !imagePath.isBlank()) {
            try {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    final double TARGET_W = 202.0;
                    final double TARGET_H = 148.0;

                    // Load at natural resolution so we can compute cover scale
                    Image img = new Image(imgFile.toURI().toString());
                    double imgW = img.getWidth();
                    double imgH = img.getHeight();

                    // cover: use the larger of the two axis scales so image fills the box
                    double scale = Math.max(TARGET_W / imgW, TARGET_H / imgH);

                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(imgW * scale);    // wider or equal to TARGET_W
                    iv.setFitHeight(imgH * scale);  // taller or equal to TARGET_H
                    iv.setPreserveRatio(false);      // already manually scaled
                    iv.setSmooth(true);

                    // StackPane centers the oversized ImageView, then clips to target size
                    StackPane imgBox = new StackPane(iv);
                    imgBox.setPrefSize(TARGET_W, TARGET_H);
                    imgBox.setMinSize(TARGET_W, TARGET_H);
                    imgBox.setMaxSize(TARGET_W, TARGET_H);
                    imgBox.setAlignment(Pos.CENTER);
                    imgBox.setStyle("-fx-background-color: #F4F4F5;");

                    // Rounded-corner clip
                    Rectangle clip = new Rectangle(TARGET_W, TARGET_H);
                    clip.setArcWidth(10);
                    clip.setArcHeight(10);
                    imgBox.setClip(clip);

                    card.getChildren().add(imgBox);
                }
            } catch (Exception ignored) { /* silently skip bad paths */ }
        }

        // ── Status badge ────────────────────────────────────────
        Label statusLabel = new Label(item.getStatus().toUpperCase());
        statusLabel.getStyleClass().addAll("card-status-badge", item.getStatus().toLowerCase());

        // ── Title ───────────────────────────────────────────────
        Label titleLabel = new Label(item.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(202);

        // ── Meta row: category + location ───────────────────────
        Label categoryLabel = new Label("📁  " + (item.getCategoryName() != null ? item.getCategoryName() : "—"));
        categoryLabel.getStyleClass().add("card-category");

        Label locationLabel = new Label("📍  " + (item.getLocationName() != null ? item.getLocationName() : "—"));
        locationLabel.getStyleClass().add("card-location");

        Label dateLabel = new Label("📅  " + item.getReportedDate().toString());
        dateLabel.getStyleClass().add("card-date");

        card.getChildren().addAll(statusLabel, titleLabel, categoryLabel, locationLabel, dateLabel);

        // ── "Found a Match?" — show on ALL lost item cards ──────────────────
        if ("lost".equalsIgnoreCase(item.getStatus()) && SessionManager.getCurrentUser() != null) {

            int reportCount = matchReportDAO.getMatchReportCount(item.getItemId());

            HBox btnRow = new HBox(6);
            btnRow.setAlignment(Pos.CENTER_LEFT);

            Button matchBtn = new Button("🔍  Found a Match?");
            matchBtn.getStyleClass().add("match-button");
            matchBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(matchBtn, javafx.scene.layout.Priority.ALWAYS);

            boolean isOwner = item.getUserId() == SessionManager.getCurrentUser().getUserId();
            boolean isAdmin = SessionManager.getCurrentUser().isAdmin();

            if (isOwner || isAdmin) {
                // Owner / admin: show count info, not clickable
                matchBtn.setText("🔍 " + reportCount + " Match Report" + (reportCount != 1 ? "s" : ""));
                matchBtn.setDisable(true);
                matchBtn.getStyleClass().clear();
                matchBtn.getStyleClass().add("match-button-done");
            } else {
                boolean alreadyReported = matchReportDAO.hasUserReportedMatch(
                        SessionManager.getCurrentUser().getUserId(), item.getItemId());
                if (alreadyReported) {
                    matchBtn.setText("✔ Reported");
                    matchBtn.setDisable(true);
                    matchBtn.getStyleClass().clear();
                    matchBtn.getStyleClass().add("match-button-done");
                } else {
                    matchBtn.setOnAction(e -> handleCardMatchReport(item, matchBtn));
                }

                if (reportCount > 0) {
                    Label countBadge = new Label(reportCount + " report" + (reportCount > 1 ? "s" : ""));
                    countBadge.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; " +
                            "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 2px 7px; " +
                            "-fx-background-radius: 20px;");
                    btnRow.getChildren().add(countBadge);
                }
            }

            btnRow.getChildren().add(0, matchBtn);
            card.getChildren().add(btnRow);
        }

        // ── Single click opens detail; double-click also works ──
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SessionManager.setCurrentSelectedItem(item);
                Main.setRoot("ItemDetail", "Lost & Found - Item Details", 800, 600);
            }
        });

        return card;
    }

    @FXML
    private void handleAddItem() {
        Main.setRoot("AddItem", "Lost & Found - Add Item", 800, 600);
    }

    @FXML
    private void handleMyClaims() {
        Main.setRoot("MyClaims", "Lost & Found - My Claims", 800, 600);
    }

    @FXML
    private void handleAdminPanel() {
        Main.setRoot("AdminPanel", "Lost & Found - Admin Panel", 1024, 768);
    }

    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        Main.setRoot("Login", "Lost & Found - Login", 800, 600);
    }

    /** Called by the "Found a Match?" button on lost item cards. */
    private void handleCardMatchReport(Item item, Button matchBtn) {
        int userId = SessionManager.getCurrentUser().getUserId();

        if (matchReportDAO.hasUserReportedMatch(userId, item.getItemId())) {
            showAlert(Alert.AlertType.WARNING, "Already Reported",
                    "You have already submitted a match report for this item.");
            return;
        }

        // Custom dialog to collect an optional note
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Report Found Match");
        dialog.setHeaderText("Reporting a potential match for:\n\"" + item.getTitle() + "\"");

        ButtonType submitType = new ButtonType("Submit Report", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10, 0, 0, 0));
        Label noteLabel = new Label("Add a note (optional) — where did you see it, description, etc.:");
        noteLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #3F3F46;");
        TextArea noteArea = new TextArea();
        noteArea.setPromptText("e.g. I saw this item near the library entrance...");
        noteArea.setPrefRowCount(3);
        noteArea.setWrapText(true);
        content.getChildren().addAll(noteLabel, noteArea);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> btn == submitType ? noteArea.getText().trim() : null);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(note -> {
            MatchReport report = new MatchReport();
            report.setItemId(item.getItemId());
            report.setReporterId(userId);
            report.setNote(note.isEmpty() ? null : note);
            report.setReportDate(Date.valueOf(LocalDate.now()));

            if (matchReportDAO.addMatchReport(report)) {
                showAlert(Alert.AlertType.INFORMATION, "Report Submitted",
                        "Your match report has been submitted successfully!");
                matchBtn.setText("✔ Reported");
                matchBtn.setDisable(true);
                matchBtn.getStyleClass().clear();
                matchBtn.getStyleClass().add("match-button-done");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to submit match report. Please try again.");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.beans.property.SimpleStringProperty;
import models.Adoption;
import models.Pet;
import services.AdoptionService;
import services.PetService;
import util.AlertBox;
import util.Session;
import util.Validator;
import java.time.LocalDate;
import java.util.List;

public class AdopterDashboardController {
    private Stage primaryStage;
    private Scene scene;
    
    private PetService petService;
    private AdoptionService adoptionService;
    
    // UI Components
    private TableView<Pet> availablePetsTable;
    private TableView<Adoption> myAdoptionsTable;
    private ObservableList<Pet> availablePetsList;
    private ObservableList<Adoption> myAdoptionsList;
    
    // Search and filter components
    private ComboBox<String> petTypeFilter;
    private TextField searchField;
    private ComboBox<String> genderFilter;
    private Slider ageSlider;
    private Label statsLabel;
    
    public AdopterDashboardController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.petService = new PetService();
        this.adoptionService = new AdoptionService();
        buildUI();
        loadData();
    }
    
    private void buildUI() {
        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: #f8fafc;");
        
        // Top menu bar
        HBox menuBar = createMenuBar();
        mainPane.setTop(menuBar);
        
        // Center - TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: white; -fx-background-radius: 10px;");
        
        // Available Pets Tab
        Tab petsTab = new Tab("🐾 Available Pets");
        petsTab.setContent(createAvailablePetsContent());
        petsTab.setClosable(false);
        
        // My Adoptions Tab
        Tab adoptionsTab = new Tab("📋 My Adoption Requests");
        adoptionsTab.setContent(createMyAdoptionsContent());
        adoptionsTab.setClosable(false);
        
        // Profile Tab
        Tab profileTab = new Tab("👤 My Profile");
        profileTab.setContent(createProfileContent());
        profileTab.setClosable(false);
        
        tabPane.getTabs().addAll(petsTab, adoptionsTab, profileTab);
        mainPane.setCenter(tabPane);
        
        scene = new Scene(mainPane, 1400, 850);
        
        // Apply stylesheet if available
        try {
            String stylesheet = getClass().getResource("/styles/Styles.css").toExternalForm();
            scene.getStylesheets().add(stylesheet);
        } catch (NullPointerException e) {
            // Use inline styles
            System.err.println("Warning: Stylesheet not found. Using inline styling.");
        }
    }
    
    private HBox createMenuBar() {
        HBox menuBar = new HBox(20);
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.setPadding(new Insets(15, 30, 15, 30));
        menuBar.setStyle("-fx-background-color: linear-gradient(to right, #4f46e5, #7c3aed);");
        
        Label appName = new Label("🐾 NexusFauna - Adopter Panel");
        appName.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        appName.setTextFill(Color.WHITE);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Notification button
        Button notificationBtn = new Button("🔔");
        notificationBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-cursor: hand;");
        notificationBtn.setOnAction(e -> showNotifications());
        notificationBtn.setTooltip(new Tooltip("View notifications"));
        
        String fullName = Session.getUserFullName();
        Label userInfo = new Label("Welcome, " + (fullName != null ? fullName : Session.getUsername()));
        userInfo.setTextFill(Color.WHITE);
        userInfo.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        Button logoutBtn = createStyledButton("Logout", "#dc2626");
        logoutBtn.setOnAction(e -> logout());
        
        menuBar.getChildren().addAll(appName, spacer, notificationBtn, userInfo, logoutBtn);
        return menuBar;
    }
    
    private VBox createAvailablePetsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #f8fafc;");
        
        // Header with search and filters
        VBox header = new VBox(15);
        header.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 20;");
        
        // Title and refresh
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Available Pets for Adoption");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#1e293b"));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshBtn = createStyledButton("🔄 Refresh List", "#64748b");
        refreshBtn.setOnAction(e -> loadAvailablePets());
        
        titleRow.getChildren().addAll(title, spacer, refreshBtn);
        
        // Search and filter bar
        GridPane filterBar = new GridPane();
        filterBar.setHgap(15);
        filterBar.setVgap(10);
        filterBar.setPadding(new Insets(15, 0, 0, 0));
        
        // Search field
        Label searchLabel = new Label("Search:");
        searchLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        searchField = new TextField();
        searchField.setPromptText("Search by name or breed...");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterPets());
        
        // Type filter
        Label typeLabel = new Label("Type:");
        typeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        petTypeFilter = new ComboBox<>();
        petTypeFilter.getItems().addAll("All Types", "Dog", "Cat", "Bird", "Rabbit", "Fish", "Other");
        petTypeFilter.setValue("All Types");
        petTypeFilter.setPrefWidth(150);
        petTypeFilter.setOnAction(e -> filterPets());
        
        // Gender filter
        Label genderLabel = new Label("Gender:");
        genderLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        genderFilter = new ComboBox<>();
        genderFilter.getItems().addAll("All Genders", "Male", "Female");
        genderFilter.setValue("All Genders");
        genderFilter.setPrefWidth(120);
        genderFilter.setOnAction(e -> filterPets());
        
        // Age filter
        Label ageLabel = new Label("Max Age:");
        ageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        HBox ageBox = new HBox(10);
        ageBox.setAlignment(Pos.CENTER_LEFT);
        
        ageSlider = new Slider(0, 20, 20);
        ageSlider.setShowTickLabels(true);
        ageSlider.setShowTickMarks(true);
        ageSlider.setMajorTickUnit(5);
        ageSlider.setMinorTickCount(4);
        ageSlider.setSnapToTicks(true);
        ageSlider.setPrefWidth(200);
        ageSlider.valueProperty().addListener((observable, oldValue, newValue) -> filterPets());
        
        Label ageValue = new Label("20 years");
        ageSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ageValue.setText(String.format("%.0f years", newVal));
        });
        
        ageBox.getChildren().addAll(ageSlider, ageValue);
        
        // Clear filters button
        Button clearFiltersBtn = createStyledButton("Clear Filters", "#94a3b8");
        clearFiltersBtn.setOnAction(e -> clearFilters());
        
        // Add components to grid
        filterBar.add(searchLabel, 0, 0);
        filterBar.add(searchField, 1, 0);
        filterBar.add(typeLabel, 2, 0);
        filterBar.add(petTypeFilter, 3, 0);
        filterBar.add(genderLabel, 4, 0);
        filterBar.add(genderFilter, 5, 0);
        filterBar.add(ageLabel, 0, 1);
        filterBar.add(ageBox, 1, 1, 3, 1);
        filterBar.add(clearFiltersBtn, 5, 1);
        
        header.getChildren().addAll(titleRow, filterBar);
        
        // Pets table
        availablePetsTable = new TableView<>();
        availablePetsTable.setStyle("-fx-background-color: white; -fx-background-radius: 10px;");
        availablePetsTable.setPlaceholder(new Label("No available pets found matching your criteria."));
        setupAvailablePetsTable();
        
        // Stats label
        statsLabel = new Label();
        statsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        statsLabel.setTextFill(Color.web("#64748b"));
        
        content.getChildren().addAll(header, availablePetsTable, statsLabel);
        return content;
    }
    
    private void setupAvailablePetsTable() {
        availablePetsTable.getColumns().clear();
        
        // Name column
        TableColumn<Pet, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);
        
        // Type column
        TableColumn<Pet, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);
        
        // Breed column
        TableColumn<Pet, String> breedCol = new TableColumn<>("Breed");
        breedCol.setCellValueFactory(new PropertyValueFactory<>("breed"));
        breedCol.setPrefWidth(150);
        
        // Age column
        TableColumn<Pet, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));
        ageCol.setPrefWidth(80);
        ageCol.setCellFactory(col -> new TableCell<Pet, Integer>() {
            @Override
            protected void updateItem(Integer age, boolean empty) {
                super.updateItem(age, empty);
                if (empty || age == null) {
                    setText(null);
                } else {
                    setText(age + " years");
                }
            }
        });
        
        // Gender column
        TableColumn<Pet, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setPrefWidth(100);
        
        // Health column
        TableColumn<Pet, String> healthCol = new TableColumn<>("Health");
        healthCol.setCellValueFactory(new PropertyValueFactory<>("healthStatus"));
        healthCol.setPrefWidth(120);
        healthCol.setCellFactory(col -> new TableCell<Pet, String>() {
            @Override
            protected void updateItem(String healthStatus, boolean empty) {
                super.updateItem(healthStatus, empty);
                if (empty || healthStatus == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(healthStatus);
                    String status = healthStatus.toLowerCase();
                    if (status.contains("excellent") || status.contains("good")) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else if (status.contains("fair")) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else if (status.contains("poor") || status.contains("critical")) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        // Vaccinated column - FIXED: Using SimpleStringProperty
        TableColumn<Pet, String> vaccinatedCol = new TableColumn<>("Vaccinated");
        vaccinatedCol.setCellValueFactory(cellData -> {
            boolean vaccinated = cellData.getValue().isVaccinated();
            String displayText = vaccinated ? "✓ Yes" : "✗ No";
            return new SimpleStringProperty(displayText);
        });
        vaccinatedCol.setPrefWidth(100);
        vaccinatedCol.setCellFactory(col -> new TableCell<Pet, String>() {
            @Override
            protected void updateItem(String vaccinated, boolean empty) {
                super.updateItem(vaccinated, empty);
                if (empty || vaccinated == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(vaccinated);
                    if (vaccinated.contains("Yes")) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #ef4444;");
                    }
                }
            }
        });
        
        // View Details column
        TableColumn<Pet, Void> viewCol = new TableColumn<>("");
        viewCol.setPrefWidth(100);
        viewCol.setCellFactory(col -> new TableCell<Pet, Void>() {
            private final Button viewBtn = new Button("👁 View");
            
            {
                viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> {
                    Pet pet = getTableView().getItems().get(getIndex());
                    viewPetDetails(pet);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });
        
        // Adopt button column
        TableColumn<Pet, Void> actionCol = new TableColumn<>("");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<Pet, Void>() {
            private final Button adoptBtn = new Button("🤍 Adopt");
            
            {
                adoptBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                adoptBtn.setOnAction(e -> {
                    Pet pet = getTableView().getItems().get(getIndex());
                    adoptPet(pet);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(adoptBtn);
                }
            }
        });
        
        availablePetsTable.getColumns().addAll(
            nameCol, typeCol, breedCol, ageCol, genderCol,
            healthCol, vaccinatedCol, viewCol, actionCol
        );
        availablePetsTable.setPrefHeight(500);
    }
    
    private VBox createMyAdoptionsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #f8fafc;");
        
        VBox card = new VBox(20);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 20;");
        
        Label title = new Label("My Adoption Requests");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#1e293b"));
        
        myAdoptionsTable = new TableView<>();
        myAdoptionsTable.setStyle("-fx-background-color: white;");
        myAdoptionsTable.setPlaceholder(new Label("You haven't made any adoption requests yet."));
        setupMyAdoptionsTable();
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        
        Button refreshBtn = createStyledButton("🔄 Refresh Requests", "#64748b");
        refreshBtn.setOnAction(e -> loadMyAdoptions());
        
        Button cancelRequestBtn = new Button("❌ Cancel Request");
        cancelRequestBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-cursor: hand;");
        cancelRequestBtn.setOnAction(e -> cancelSelectedAdoption());
        cancelRequestBtn.setDisable(true);
        
        myAdoptionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            cancelRequestBtn.setDisable(newSelection == null || !"Pending".equals(newSelection.getStatus()));
        });
        
        buttonBox.getChildren().addAll(refreshBtn, cancelRequestBtn);
        
        card.getChildren().addAll(title, myAdoptionsTable, buttonBox);
        content.getChildren().add(card);
        return content;
    }
    
    private void setupMyAdoptionsTable() {
        myAdoptionsTable.getColumns().clear();
        
        TableColumn<Adoption, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        
        // Pet column - FIXED: Using SimpleStringProperty
        TableColumn<Adoption, String> petCol = new TableColumn<>("Pet");
        petCol.setCellValueFactory(cellData -> {
            Pet pet = cellData.getValue().getPet();
            String petName = pet != null ? pet.getName() : "Unknown";
            return new SimpleStringProperty(petName);
        });
        petCol.setPrefWidth(150);
        
        // Type column - FIXED: Using SimpleStringProperty
        TableColumn<Adoption, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> {
            Pet pet = cellData.getValue().getPet();
            String petType = pet != null ? pet.getType() : "Unknown";
            return new SimpleStringProperty(petType);
        });
        typeCol.setPrefWidth(100);
        
        TableColumn<Adoption, String> dateCol = new TableColumn<>("Request Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("adoptionDate"));
        dateCol.setPrefWidth(120);
        
        TableColumn<Adoption, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<Adoption, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "Pending":
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-background-color: #fef3c7; -fx-padding: 5; -fx-background-radius: 5;");
                            break;
                        case "Approved":
                            setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-background-color: #d1fae5; -fx-padding: 5; -fx-background-radius: 5;");
                            break;
                        case "Rejected":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-background-color: #fee2e2; -fx-padding: 5; -fx-background-radius: 5;");
                            break;
                        case "Completed":
                            setStyle("-fx-text-fill: #8b5cf6; -fx-font-weight: bold; -fx-background-color: #ede9fe; -fx-padding: 5; -fx-background-radius: 5;");
                            break;
                        case "Cancelled":
                            setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-background-color: #f1f5f9; -fx-padding: 5; -fx-background-radius: 5;");
                            break;
                    }
                }
            }
        });
        
        TableColumn<Adoption, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesCol.setPrefWidth(300);
        
        myAdoptionsTable.getColumns().addAll(idCol, petCol, typeCol, dateCol, statusCol, notesCol);
        myAdoptionsTable.setPrefHeight(400);
    }
    
    private VBox createProfileContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: #f8fafc;");
        
        Label title = new Label("My Profile");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));
        
        HBox profileContainer = new HBox(30);
        profileContainer.setAlignment(Pos.TOP_CENTER);
        
        // Left side - Profile info
        VBox profileCard = new VBox(25);
        profileCard.setAlignment(Pos.CENTER);
        profileCard.setPrefWidth(400);
        profileCard.setPadding(new Insets(30));
        profileCard.setStyle("-fx-background-color: white; -fx-background-radius: 15px; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        
        Label profileIcon = new Label("👤");
        profileIcon.setFont(Font.font("Arial", 64));
        
        String fullName = Session.getUserFullName();
        Label nameLabel = new Label(fullName != null ? fullName : "User");
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        nameLabel.setTextFill(Color.web("#1e293b"));
        
        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        infoBox.setPadding(new Insets(20, 0, 0, 0));
        
        addInfoRow(infoBox, "Username", Session.getUsername());
        addInfoRow(infoBox, "Email", Session.getUserEmail());
        addInfoRow(infoBox, "Role", "Adopter");
        
        // Right side - Statistics
        VBox statsCard = new VBox(20);
        statsCard.setAlignment(Pos.TOP_CENTER);
        statsCard.setPrefWidth(400);
        statsCard.setPadding(new Insets(30));
        statsCard.setStyle("-fx-background-color: white; -fx-background-radius: 15px; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        
        Label statsTitle = new Label("Adoption Statistics");
        statsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        statsTitle.setTextFill(Color.web("#1e293b"));
        
        try {
            AdoptionService.AdopterStats stats = adoptionService.getAdopterStats(Session.getUserId());
            
            GridPane statsGrid = new GridPane();
            statsGrid.setHgap(20);
            statsGrid.setVgap(15);
            statsGrid.setPadding(new Insets(20, 0, 0, 0));
            
            addStatCard(statsGrid, 0, 0, "Total Requests", String.valueOf(stats.getTotalRequests()), "#4f46e5");
            addStatCard(statsGrid, 1, 0, "Pending", String.valueOf(stats.getPendingRequests()), "#f59e0b");
            addStatCard(statsGrid, 0, 1, "Approved", String.valueOf(stats.getApprovedRequests()), "#10b981");
            addStatCard(statsGrid, 1, 1, "Rejected", String.valueOf(stats.getRejectedRequests()), "#ef4444");
            
            statsCard.getChildren().addAll(statsTitle, statsGrid);
            
            // Last request info
            if (stats.getTotalRequests() > 0) {
                List<Adoption> adoptions = adoptionService.getAdopterAdoptions(Session.getUserId());
                if (!adoptions.isEmpty()) {
                    Adoption lastAdoption = adoptions.get(0);
                    VBox lastRequestBox = new VBox(10);
                    lastRequestBox.setPadding(new Insets(20, 0, 0, 0));
                    lastRequestBox.setAlignment(Pos.CENTER_LEFT);
                    
                    Label lastRequestTitle = new Label("Last Request:");
                    lastRequestTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                    
                    String petName = lastAdoption.getPet() != null ? lastAdoption.getPet().getName() : "Unknown";
                    Label lastRequestInfo = new Label(petName + " - " + lastAdoption.getStatus() + 
                                                     " (" + lastAdoption.getAdoptionDate() + ")");
                    lastRequestInfo.setFont(Font.font("Arial", 14));
                    
                    lastRequestBox.getChildren().addAll(lastRequestTitle, lastRequestInfo);
                    statsCard.getChildren().add(lastRequestBox);
                }
            }
        } catch (Exception e) {
            Label errorLabel = new Label("Unable to load statistics");
            errorLabel.setTextFill(Color.web("#ef4444"));
            statsCard.getChildren().add(errorLabel);
        }
        
        profileCard.getChildren().addAll(profileIcon, nameLabel, infoBox);
        profileContainer.getChildren().addAll(profileCard, statsCard);
        
        content.getChildren().addAll(title, profileContainer);
        return content;
    }
    
    private void addInfoRow(VBox container, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelText = new Label(label + ":");
        labelText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        labelText.setMinWidth(100);
        labelText.setTextFill(Color.web("#64748b"));
        
        Label valueText = new Label(value != null ? value : "N/A");
        valueText.setFont(Font.font("Arial", 14));
        valueText.setTextFill(Color.web("#1e293b"));
        
        row.getChildren().addAll(labelText, valueText);
        container.getChildren().add(row);
    }
    
    private void addStatCard(GridPane grid, int col, int row, String title, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 10px;",
            color
        ));
        card.setPrefWidth(150);
        card.setPrefHeight(80);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueLabel.setTextFill(Color.WHITE);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setTextFill(Color.WHITE);
        
        card.getChildren().addAll(valueLabel, titleLabel);
        grid.add(card, col, row);
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 10 20; -fx-background-radius: 8px; -fx-cursor: hand;",
            color
        ));
        button.setOnMouseEntered(e -> button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 10 20; -fx-background-radius: 8px; -fx-cursor: hand; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 8, 0, 0, 3);",
            darkenColor(color, 0.1)
        )));
        button.setOnMouseExited(e -> button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-padding: 10 20; -fx-background-radius: 8px; -fx-cursor: hand;",
            color
        )));
        return button;
    }
    
    private String darkenColor(String hexColor, double factor) {
        try {
            Color color = Color.web(hexColor);
            return String.format("#%02x%02x%02x",
                (int)(color.getRed() * 255 * (1 - factor)),
                (int)(color.getGreen() * 255 * (1 - factor)),
                (int)(color.getBlue() * 255 * (1 - factor)));
        } catch (Exception e) {
            return hexColor;
        }
    }
    
    private void loadData() {
        loadAvailablePets();
        loadMyAdoptions();
    }
    
    private void loadAvailablePets() {
        try {
            List<Pet> pets = petService.getAvailablePets();
            if (pets != null) {
                availablePetsList = FXCollections.observableArrayList(pets);
                availablePetsTable.setItems(availablePetsList);
                updateStatsLabel();
            }
        } catch (Exception e) {
            System.err.println("Error loading available pets: " + e.getMessage());
            AlertBox.showError("Error", "Failed to load available pets: " + e.getMessage());
        }
    }
    
    private void loadMyAdoptions() {
        try {
            int userId = Session.getUserId();
            if (userId > 0) {
                List<Adoption> adoptions = adoptionService.getAdopterAdoptions(userId);
                if (adoptions != null) {
                    myAdoptionsList = FXCollections.observableArrayList(adoptions);
                    myAdoptionsTable.setItems(myAdoptionsList);
                }
            } else {
                AlertBox.showError("Error", "Invalid user session. Please login again.");
            }
        } catch (Exception e) {
            System.err.println("Error loading adoptions: " + e.getMessage());
            AlertBox.showError("Error", "Failed to load your adoption requests: " + e.getMessage());
        }
    }
    
    private void filterPets() {
        try {
            String searchTerm = searchField.getText();
            String type = petTypeFilter.getValue().equals("All Types") ? null : petTypeFilter.getValue();
            String gender = genderFilter.getValue().equals("All Genders") ? null : genderFilter.getValue();
            int maxAge = (int) ageSlider.getValue();
            
            // Call new search method in PetService
            List<Pet> filteredPets = petService.searchPets(searchTerm, type, gender, maxAge);
            availablePetsList = FXCollections.observableArrayList(filteredPets);
            availablePetsTable.setItems(availablePetsList);
            updateStatsLabel();
        } catch (Exception e) {
            System.err.println("Error filtering pets: " + e.getMessage());
        }
    }
    
    private void clearFilters() {
        searchField.clear();
        petTypeFilter.setValue("All Types");
        genderFilter.setValue("All Genders");
        ageSlider.setValue(20);
        loadAvailablePets();
    }
    
    private void updateStatsLabel() {
        int count = availablePetsList != null ? availablePetsList.size() : 0;
        statsLabel.setText("Showing " + count + " available pets");
    }
    
    private void viewPetDetails(Pet pet) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Pet Details - " + pet.getName());
        dialog.initStyle(StageStyle.UTILITY);
        
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setStyle("-fx-background-color: white;");
        
        Label title = new Label(pet.getName());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#4f46e5"));
        
        // Pet image placeholder
        Label petIcon = new Label(getPetEmoji(pet.getType()));
        petIcon.setFont(Font.font("Arial", 72));
        
        VBox details = new VBox(10);
        details.setAlignment(Pos.CENTER_LEFT);
        details.setPadding(new Insets(20));
        details.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10px;");
        
        addDetailRow(details, "Type", pet.getType());
        addDetailRow(details, "Breed", pet.getBreed());
        addDetailRow(details, "Age", pet.getAge() + " years");
        addDetailRow(details, "Gender", pet.getGender());
        addDetailRow(details, "Health Status", pet.getHealthStatus());
        addDetailRow(details, "Vaccinated", pet.isVaccinated() ? "Yes" : "No");
        addDetailRow(details, "Status", pet.getStatus());
        
        if (pet.getDescription() != null && !pet.getDescription().trim().isEmpty()) {
            Label descTitle = new Label("Description:");
            descTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            descTitle.setTextFill(Color.web("#64748b"));
            
            TextArea descArea = new TextArea(pet.getDescription());
            descArea.setEditable(false);
            descArea.setWrapText(true);
            descArea.setPrefHeight(100);
            descArea.setStyle("-fx-background-color: transparent; -fx-border-color: #e2e8f0;");
            
            details.getChildren().addAll(descTitle, descArea);
        }
        
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button adoptBtn = createStyledButton("Adopt This Pet", "#10b981");
        adoptBtn.setOnAction(e -> {
            dialog.close();
            adoptPet(pet);
        });
        
        Button closeBtn = createStyledButton("Close", "#64748b");
        closeBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(adoptBtn, closeBtn);
        
        dialogContent.getChildren().addAll(title, petIcon, details, buttonBox);
        
        Scene dialogScene = new Scene(dialogContent, 500, 600);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    
    private String getPetEmoji(String type) {
        if (type == null) return "🐾";
        switch (type.toLowerCase()) {
            case "dog": return "🐕";
            case "cat": return "🐈";
            case "bird": return "🐦";
            case "rabbit": return "🐇";
            case "fish": return "🐠";
            default: return "🐾";
        }
    }
    
    private void addDetailRow(VBox container, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label labelText = new Label(label + ":");
        labelText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        labelText.setMinWidth(120);
        labelText.setTextFill(Color.web("#64748b"));
        
        Label valueText = new Label(value != null ? value : "N/A");
        valueText.setFont(Font.font("Arial", 14));
        valueText.setTextFill(Color.web("#1e293b"));
        
        row.getChildren().addAll(labelText, valueText);
        container.getChildren().add(row);
    }
    
    private void adoptPet(Pet pet) {
        // Check if user already has a pending request
        try {
            if (adoptionService.hasPendingRequest(Session.getUserId(), pet.getId())) {
                AlertBox.showError("Duplicate Request", 
                    "You already have a pending adoption request for " + pet.getName() + 
                    ". Please wait for it to be processed.");
                return;
            }
        } catch (Exception e) {
            System.err.println("Error checking pending request: " + e.getMessage());
        }
        
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Adopt " + pet.getName());
        dialog.initStyle(StageStyle.UTILITY);
        
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setStyle("-fx-background-color: white;");
        
        Label title = new Label("Adopt " + pet.getName());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(Color.web("#1e293b"));
        
        // Pet info
        VBox petInfo = new VBox(10);
        petInfo.setAlignment(Pos.CENTER_LEFT);
        petInfo.setPadding(new Insets(15));
        petInfo.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10px;");
        
        addDetailRow(petInfo, "Type", pet.getType());
        addDetailRow(petInfo, "Breed", pet.getBreed());
        addDetailRow(petInfo, "Age", pet.getAge() + " years");
        addDetailRow(petInfo, "Gender", pet.getGender());
        addDetailRow(petInfo, "Health", pet.getHealthStatus());
        addDetailRow(petInfo, "Vaccinated", pet.isVaccinated() ? "Yes" : "No");
        
        // Notes field
        Label notesLabel = new Label("Why do you want to adopt this pet?");
        notesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        notesLabel.setTextFill(Color.web("#1e293b"));
        
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Tell us about your home, experience with pets, why you want to adopt, etc.");
        notesArea.setPrefHeight(100);
        notesArea.setPrefWidth(350);
        notesArea.setWrapText(true);
        notesArea.setStyle("-fx-border-color: #e2e8f0; -fx-border-radius: 5px;");
        
        // Validation label
        Label validationLabel = new Label("Minimum 20 characters required");
        validationLabel.setFont(Font.font("Arial", 11));
        validationLabel.setTextFill(Color.web("#64748b"));
        
        notesArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 20) {
                validationLabel.setTextFill(Color.web("#10b981"));
                validationLabel.setText("✓ " + newValue.length() + " characters (good)");
            } else {
                validationLabel.setTextFill(Color.web("#f59e0b"));
                validationLabel.setText("⚠ " + newValue.length() + "/20 characters");
            }
        });
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button submitBtn = createStyledButton("Submit Request", "#10b981");
        submitBtn.setDisable(true);
        
        notesArea.textProperty().addListener((observable, oldValue, newValue) -> {
            submitBtn.setDisable(newValue.trim().length() < 20);
        });
        
        submitBtn.setOnAction(e -> {
            String notes = notesArea.getText().trim();
            if (notes.length() < 20) {
                AlertBox.showError("Error", "Please provide more detailed notes (at least 20 characters)");
                return;
            }
            
            try {
                String result = adoptionService.requestAdoption(pet.getId(), Session.getUserId(), notes);
                
                if (result.contains("successfully")) {
                    AlertBox.showInfo("Success", result);
                    dialog.close();
                    loadData(); // Refresh both tables
                } else {
                    AlertBox.showError("Error", result);
                }
            } catch (Exception ex) {
                AlertBox.showError("Error", "Failed to submit adoption request: " + ex.getMessage());
            }
        });
        
        Button cancelBtn = createStyledButton("Cancel", "#64748b");
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(submitBtn, cancelBtn);
        
        dialogContent.getChildren().addAll(title, petInfo, notesLabel, notesArea, validationLabel, buttonBox);
        
        Scene dialogScene = new Scene(dialogContent, 500, 550);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    
    private void cancelSelectedAdoption() {
        Adoption selectedAdoption = myAdoptionsTable.getSelectionModel().getSelectedItem();
        if (selectedAdoption == null) {
            AlertBox.showError("Error", "Please select an adoption request to cancel.");
            return;
        }
        
        if (!"Pending".equals(selectedAdoption.getStatus())) {
            AlertBox.showError("Error", "Only pending adoption requests can be cancelled.");
            return;
        }
        
        String petName = selectedAdoption.getPet() != null ? 
            selectedAdoption.getPet().getName() : "this pet";
        
        boolean confirm = AlertBox.showConfirmation(
            "Cancel Adoption Request",
            "Are you sure you want to cancel your adoption request for " + petName + "?"
        );
        
        if (confirm) {
            try {
                boolean success = adoptionService.cancelAdoption(selectedAdoption.getId());
                if (success) {
                    AlertBox.showInfo("Success", "Adoption request cancelled successfully.");
                    loadMyAdoptions();
                    loadAvailablePets(); // Refresh available pets list
                } else {
                    AlertBox.showError("Error", "Failed to cancel adoption request.");
                }
            } catch (Exception e) {
                AlertBox.showError("Error", "An error occurred: " + e.getMessage());
            }
        }
    }
    
    private void showNotifications() {
        try {
            List<Adoption> adoptions = adoptionService.getAdopterAdoptions(Session.getUserId());
            long pendingCount = adoptions.stream().filter(a -> "Pending".equals(a.getStatus())).count();
            
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(primaryStage);
            dialog.setTitle("Notifications");
            
            VBox content = new VBox(20);
            content.setPadding(new Insets(30));
            content.setAlignment(Pos.CENTER);
            
            Label title = new Label("🔔 Notifications");
            title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            
            VBox notifications = new VBox(10);
            notifications.setAlignment(Pos.CENTER_LEFT);
            
            if (pendingCount > 0) {
                Label pendingLabel = new Label("• You have " + pendingCount + " pending adoption request(s)");
                pendingLabel.setFont(Font.font("Arial", 14));
                pendingLabel.setTextFill(Color.web("#f59e0b"));
                notifications.getChildren().add(pendingLabel);
            }
            
            long approvedCount = adoptions.stream().filter(a -> "Approved".equals(a.getStatus())).count();
            if (approvedCount > 0) {
                Label approvedLabel = new Label("• You have " + approvedCount + " approved adoption(s)");
                approvedLabel.setFont(Font.font("Arial", 14));
                approvedLabel.setTextFill(Color.web("#10b981"));
                notifications.getChildren().add(approvedLabel);
            }
            
            if (notifications.getChildren().isEmpty()) {
                Label noNotifications = new Label("No new notifications");
                noNotifications.setFont(Font.font("Arial", 14));
                noNotifications.setTextFill(Color.web("#64748b"));
                notifications.getChildren().add(noNotifications);
            }
            
            Button closeBtn = createStyledButton("Close", "#64748b");
            closeBtn.setOnAction(e -> dialog.close());
            
            content.getChildren().addAll(title, notifications, closeBtn);
            
            Scene dialogScene = new Scene(content, 400, 300);
            dialog.setScene(dialogScene);
            dialog.showAndWait();
            
        } catch (Exception e) {
            AlertBox.showError("Error", "Failed to load notifications: " + e.getMessage());
        }
    }
    
    private void logout() {
        boolean confirm = AlertBox.showConfirmation("Logout", "Are you sure you want to logout?");
        if (confirm) {
            Session.clear();
            LoginController loginController = new LoginController(primaryStage);
            primaryStage.setScene(loginController.getScene());
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
            AlertBox.showInfo("Logged Out", "You have been successfully logged out.");
        }
    }
    
    public Scene getScene() {
        return scene;
    }
}
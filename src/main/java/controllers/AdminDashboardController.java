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
import models.Adoption;
import models.Pet;
import services.AdoptionService;
import services.PetService;
import util.AlertBox;
import util.Session;
import util.Validator;
import java.time.LocalDate;
import java.util.List;

public class AdminDashboardController {
    private Stage primaryStage;
    private Scene scene;
    
    private PetService petService;
    private AdoptionService adoptionService;
    
    // UI Components
    private TabPane tabPane;
    private TableView<Pet> petsTable;
    private TableView<Adoption> adoptionsTable;
    private ObservableList<Pet> petList;
    private ObservableList<Adoption> adoptionList;
    
    // Stats labels
    private Label totalPetsLabel;
    private Label availablePetsLabel;
    private Label pendingAdoptionsLabel;
    private Label adoptedPetsLabel;
    
    public AdminDashboardController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.petService = new PetService();
        this.adoptionService = new AdoptionService();
        buildUI();
        loadData();
    }
    
    private void buildUI() {
        // Main BorderPane
        BorderPane mainPane = new BorderPane();
        mainPane.getStyleClass().add("root");
        
        // Top Menu Bar
        HBox menuBar = createMenuBar();
        mainPane.setTop(menuBar);
        
        // Center - TabPane
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Dashboard Tab
        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setContent(createDashboardContent());
        
        // Pets Management Tab
        Tab petsTab = new Tab("Manage Pets");
        petsTab.setContent(createPetsContent());
        
        // Adoptions Tab
        Tab adoptionsTab = new Tab("Adoption Requests");
        adoptionsTab.setContent(createAdoptionsContent());
        
        // Reports Tab
        Tab reportsTab = new Tab("Reports");
        reportsTab.setContent(createReportsContent());
        
        tabPane.getTabs().addAll(dashboardTab, petsTab, adoptionsTab, reportsTab);
        mainPane.setCenter(tabPane);
        
        scene = new Scene(mainPane, 1400, 850);
        
        // FIX: Check if stylesheet exists before adding it
        try {
            String stylesheet = getClass().getResource("/styles/Styles.css").toExternalForm();
            scene.getStylesheets().add(stylesheet);
        } catch (NullPointerException e) {
            System.err.println("Warning: Stylesheet not found. Application will run without custom styles.");
        }
    }
    
    private HBox createMenuBar() {
        HBox menuBar = new HBox(20);
        menuBar.getStyleClass().add("dashboard-header");
        menuBar.setAlignment(Pos.CENTER_LEFT);
        menuBar.setPadding(new Insets(15, 20, 15, 20)); // Added padding
        menuBar.setStyle("-fx-background-color: #1e293b;"); // Added background color
        
        // App name
        Label appName = new Label("NexusFauna - Admin Panel");
        appName.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        appName.setTextFill(Color.WHITE);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // User info
        String fullName = Session.getUserFullName();
        Label userInfo = new Label("Welcome, " + (fullName != null ? fullName : "Admin"));
        userInfo.setTextFill(Color.WHITE);
        userInfo.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        // Logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold;");
        logoutBtn.setOnAction(e -> logout());
        
        menuBar.getChildren().addAll(appName, spacer, userInfo, logoutBtn);
        return menuBar;
    }
    
    private VBox createDashboardContent() {
        VBox dashboard = new VBox(30);
        dashboard.setPadding(new Insets(30));
        dashboard.setAlignment(Pos.TOP_CENTER);
        
        // Title
        Label title = new Label("Dashboard Overview");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));
        
        // Stats cards
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);
        
        totalPetsLabel = createStatLabel("0");
        availablePetsLabel = createStatLabel("0");
        pendingAdoptionsLabel = createStatLabel("0");
        adoptedPetsLabel = createStatLabel("0");
        
        VBox totalCard = createStatCard("Total Pets", totalPetsLabel, "#4f46e5");
        VBox availableCard = createStatCard("Available Pets", availablePetsLabel, "#10b981");
        VBox pendingCard = createStatCard("Pending Requests", pendingAdoptionsLabel, "#f59e0b");
        VBox adoptedCard = createStatCard("Adopted Pets", adoptedPetsLabel, "#ef4444");
        
        statsRow.getChildren().addAll(totalCard, availableCard, pendingCard, adoptedCard);
        
        // Quick actions
        Label actionsLabel = new Label("Quick Actions");
        actionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        actionsLabel.setTextFill(Color.web("#1e293b"));
        
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button addPetBtn = createStyledButton("Add New Pet", "#10b981");
        addPetBtn.setOnAction(e -> showAddPetDialog());
        
        Button viewRequestsBtn = createStyledButton("View Adoption Requests", "#4f46e5");
        viewRequestsBtn.setOnAction(e -> tabPane.getSelectionModel().select(2));
        
        Button refreshBtn = createStyledButton("Refresh Dashboard", "#64748b");
        refreshBtn.setOnAction(e -> loadData());
        
        actionButtons.getChildren().addAll(addPetBtn, viewRequestsBtn, refreshBtn);
        
        dashboard.getChildren().addAll(title, statsRow, actionsLabel, actionButtons);
        return dashboard;
    }
    
    private Label createStatLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        label.setTextFill(Color.WHITE);
        return label;
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;",
            color
        ));
        button.setOnMouseEntered(e -> button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 5, 0, 0, 2);",
            color
        )));
        button.setOnMouseExited(e -> button.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;",
            color
        )));
        return button;
    }
    
    private VBox createStatCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle(String.format(
            "-fx-background-color: %s; -fx-background-radius: 12px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);",
            color
        ));
        card.setPrefWidth(200);
        card.setPrefHeight(120);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.WHITE);
        
        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }
    
    private VBox createPetsContent() {
        VBox petsContent = new VBox(20);
        petsContent.setPadding(new Insets(20));
        
        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Manage Pets");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addBtn = createStyledButton("+ Add New Pet", "#10b981");
        addBtn.setOnAction(e -> showAddPetDialog());
        
        Button refreshBtn = createStyledButton("Refresh", "#64748b");
        refreshBtn.setOnAction(e -> loadPets());
        
        header.getChildren().addAll(title, spacer, addBtn, refreshBtn);
        
        // Pets table
        petsTable = new TableView<>();
        petsTable.setPlaceholder(new Label("No pets found. Click 'Add New Pet' to add one."));
        setupPetsTable();
        
        petsContent.getChildren().addAll(header, petsTable);
        return petsContent;
    }
    
    private void setupPetsTable() {
        // Clear existing columns to avoid duplicates
        petsTable.getColumns().clear();
        
        // ID column
        TableColumn<Pet, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        
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
        
        // Gender column (Added missing column)
        TableColumn<Pet, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setPrefWidth(100);
        
        // Status column
        TableColumn<Pet, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<Pet, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "Available":
                            setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                            break;
                        case "Pending":
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            break;
                        case "Adopted":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #64748b;");
                    }
                }
            }
        });
        
        // Actions column
        TableColumn<Pet, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<Pet, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(10, editBtn, deleteBtn);
            
            {
                buttons.setAlignment(Pos.CENTER);
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");
                deleteBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white;");
                
                editBtn.setOnAction(e -> {
                    Pet pet = getTableView().getItems().get(getIndex());
                    editPet(pet);
                });
                
                deleteBtn.setOnAction(e -> {
                    Pet pet = getTableView().getItems().get(getIndex());
                    deletePet(pet);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
        
        petsTable.getColumns().addAll(idCol, nameCol, typeCol, breedCol, ageCol, genderCol, statusCol, actionsCol);
        petsTable.setPrefHeight(500);
    }
    
    private VBox createAdoptionsContent() {
        VBox adoptionsContent = new VBox(20);
        adoptionsContent.setPadding(new Insets(20));
        
        Label title = new Label("Pending Adoption Requests");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        adoptionsTable = new TableView<>();
        adoptionsTable.setPlaceholder(new Label("No pending adoption requests found."));
        setupAdoptionsTable();
        
        Button refreshBtn = createStyledButton("Refresh Requests", "#64748b");
        refreshBtn.setOnAction(e -> loadAdoptions());
        
        adoptionsContent.getChildren().addAll(title, adoptionsTable, refreshBtn);
        return adoptionsContent;
    }
    
    private void setupAdoptionsTable() {
        // Clear existing columns
        adoptionsTable.getColumns().clear();
        
        // ID column
        TableColumn<Adoption, Integer> idCol = new TableColumn<>("Request ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(100);
        
        // Pet column - FIX: Handle null pet
        TableColumn<Adoption, String> petCol = new TableColumn<>("Pet");
        petCol.setCellValueFactory(cellData -> {
            Pet pet = cellData.getValue().getPet();
            return pet != null ? pet.nameProperty() : null;
        });
        petCol.setPrefWidth(150);
        
        // Adopter column - FIX: Handle null adopter
        TableColumn<Adoption, String> adopterCol = new TableColumn<>("Adopter");
        adopterCol.setCellValueFactory(cellData -> {
            models.User adopter = cellData.getValue().getAdopter();
            return adopter != null ? adopter.fullNameProperty() : null;
        });
        adopterCol.setPrefWidth(150);
        
        // Date column
        TableColumn<Adoption, String> dateCol = new TableColumn<>("Request Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("adoptionDate"));
        dateCol.setPrefWidth(120);
        
        // Status column (Added for completeness)
        TableColumn<Adoption, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        
        // Notes column
        TableColumn<Adoption, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesCol.setPrefWidth(200);
        
        // Actions column
        TableColumn<Adoption, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(250);
        actionsCol.setCellFactory(col -> new TableCell<Adoption, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox buttons = new HBox(10, viewBtn, approveBtn, rejectBtn);
            
            {
                buttons.setAlignment(Pos.CENTER);
                viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;");
                approveBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white;");
                rejectBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white;");
                
                viewBtn.setOnAction(e -> {
                    Adoption adoption = getTableView().getItems().get(getIndex());
                    viewAdoptionDetails(adoption);
                });
                
                approveBtn.setOnAction(e -> {
                    Adoption adoption = getTableView().getItems().get(getIndex());
                    processAdoption(adoption, true);
                });
                
                rejectBtn.setOnAction(e -> {
                    Adoption adoption = getTableView().getItems().get(getIndex());
                    processAdoption(adoption, false);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
        
        adoptionsTable.getColumns().addAll(idCol, petCol, adopterCol, dateCol, statusCol, notesCol, actionsCol);
        adoptionsTable.setPrefHeight(500);
    }
    
    private VBox createReportsContent() {
        VBox reportsContent = new VBox(20);
        reportsContent.setPadding(new Insets(30));
        reportsContent.setAlignment(Pos.CENTER);
        
        Label title = new Label("System Reports");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Report options
        VBox reportOptions = new VBox(15);
        reportOptions.setAlignment(Pos.CENTER);
        reportOptions.setPrefWidth(400);
        
        Button petsReportBtn = createStyledButton("Generate Pets Report", "#4f46e5");
        petsReportBtn.setPrefWidth(300);
        petsReportBtn.setOnAction(e -> generatePetsReport());
        
        Button adoptionsReportBtn = createStyledButton("Generate Adoptions Report", "#4f46e5");
        adoptionsReportBtn.setPrefWidth(300);
        adoptionsReportBtn.setOnAction(e -> generateAdoptionsReport());
        
        Button usersReportBtn = createStyledButton("Generate Users Report", "#4f46e5");
        usersReportBtn.setPrefWidth(300);
        usersReportBtn.setOnAction(e -> generateUsersReport());
        
        reportOptions.getChildren().addAll(petsReportBtn, adoptionsReportBtn, usersReportBtn);
        
        reportsContent.getChildren().addAll(title, reportOptions);
        return reportsContent;
    }
    
    private void loadData() {
        loadStats();
        loadPets();
        loadAdoptions();
    }
    
    private void loadStats() {
        try {
            totalPetsLabel.setText(String.valueOf(petService.getTotalPetsCount()));
            availablePetsLabel.setText(String.valueOf(petService.getAvailablePetsCount()));
            adoptedPetsLabel.setText(String.valueOf(petService.getAdoptedPetsCount()));
            
            List<Adoption> pending = adoptionService.getPendingAdoptions();
            pendingAdoptionsLabel.setText(String.valueOf(pending != null ? pending.size() : 0));
        } catch (Exception e) {
            System.err.println("Error loading stats: " + e.getMessage());
        }
    }
    
    private void loadPets() {
        try {
            List<Pet> pets = petService.getAllPets();
            if (pets != null) {
                petList = FXCollections.observableArrayList(pets);
                petsTable.setItems(petList);
            }
        } catch (Exception e) {
            System.err.println("Error loading pets: " + e.getMessage());
            AlertBox.showError("Error", "Failed to load pets: " + e.getMessage());
        }
    }
    
    private void loadAdoptions() {
        try {
            List<Adoption> adoptions = adoptionService.getPendingAdoptions();
            if (adoptions != null) {
                adoptionList = FXCollections.observableArrayList(adoptions);
                adoptionsTable.setItems(adoptionList);
            }
        } catch (Exception e) {
            System.err.println("Error loading adoptions: " + e.getMessage());
            AlertBox.showError("Error", "Failed to load adoption requests: " + e.getMessage());
        }
    }
    
    private void showAddPetDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage); // Set owner for proper positioning
        dialog.setTitle("Add New Pet");
        
        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setAlignment(Pos.CENTER);
        
        Label title = new Label("Add New Pet");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        // Form fields
        TextField nameField = new TextField();
        nameField.setPromptText("Pet Name");
        nameField.setPrefWidth(300);
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Dog", "Cat", "Bird", "Rabbit", "Fish", "Other");
        typeCombo.setPromptText("Select Type");
        typeCombo.setPrefWidth(300);
        
        TextField breedField = new TextField();
        breedField.setPromptText("Breed");
        breedField.setPrefWidth(300);
        
        TextField ageField = new TextField();
        ageField.setPromptText("Age (years)");
        ageField.setPrefWidth(300);
        
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female", "Unknown");
        genderCombo.setPromptText("Select Gender");
        genderCombo.setPrefWidth(300);
        
        ComboBox<String> healthCombo = new ComboBox<>();
        healthCombo.getItems().addAll("Excellent", "Good", "Fair", "Poor", "Critical");
        healthCombo.setValue("Good");
        healthCombo.setPromptText("Health Status");
        healthCombo.setPrefWidth(300);
        
        CheckBox vaccinatedCheck = new CheckBox("Vaccinated");
        
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefHeight(100);
        descArea.setPrefWidth(300);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button saveBtn = createStyledButton("Save", "#10b981");
        saveBtn.setOnAction(e -> {
            String error = Validator.validatePet(
                nameField.getText(), 
                typeCombo.getValue(),
                ageField.getText(),
                genderCombo.getValue(),
                healthCombo.getValue()
            );
            
            if (error != null) {
                AlertBox.showError("Validation Error", error);
                return;
            }
            
            try {
                Pet pet = new Pet(
                    nameField.getText(),
                    typeCombo.getValue(),
                    breedField.getText(),
                    Integer.parseInt(ageField.getText()),
                    genderCombo.getValue(),
                    healthCombo.getValue(),
                    vaccinatedCheck.isSelected(),
                    descArea.getText()
                );
                
                boolean success = petService.addPet(pet);
                if (success) {
                    AlertBox.showInfo("Success", "Pet added successfully!");
                    dialog.close();
                    loadData();
                } else {
                    AlertBox.showError("Error", "Failed to add pet");
                }
            } catch (NumberFormatException ex) {
                AlertBox.showError("Validation Error", "Age must be a valid number.");
            }
        });
        
        Button cancelBtn = createStyledButton("Cancel", "#64748b");
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        
        dialogContent.getChildren().addAll(
            title,
            nameField, typeCombo, breedField, ageField,
            genderCombo, healthCombo, vaccinatedCheck,
            new Label("Description:"), descArea, buttonBox
        );
        
        Scene dialogScene = new Scene(dialogContent, 400, 650);
        
        // Try to load stylesheet
        try {
            String stylesheet = getClass().getResource("/styles/Styles.css").toExternalForm();
            dialogScene.getStylesheets().add(stylesheet);
        } catch (Exception ex) {
            // Use inline styles if stylesheet not found
            dialogContent.setStyle("-fx-background-color: #f8fafc;");
        }
        
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    
    private void editPet(Pet pet) {
        // Show a simple edit dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Edit Pet - " + pet.getName());
        
        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new Insets(30));
        
        Label title = new Label("Edit Pet Information");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        TextField nameField = new TextField(pet.getName());
        TextField breedField = new TextField(pet.getBreed());
        TextField ageField = new TextField(String.valueOf(pet.getAge()));
        TextArea descArea = new TextArea(pet.getDescription());
        descArea.setPrefHeight(100);
        
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Available", "Pending", "Adopted");
        statusCombo.setValue(pet.getStatus());
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button saveBtn = createStyledButton("Save Changes", "#10b981");
        Button cancelBtn = createStyledButton("Cancel", "#64748b");
        
        saveBtn.setOnAction(e -> {
            // Update pet information
            pet.setName(nameField.getText());
            pet.setBreed(breedField.getText());
            try {
                pet.setAge(Integer.parseInt(ageField.getText()));
            } catch (NumberFormatException ex) {
                AlertBox.showError("Error", "Age must be a valid number.");
                return;
            }
            pet.setDescription(descArea.getText());
            pet.setStatus(statusCombo.getValue());
            
            // Save to database
            boolean success = petService.updatePet(pet);
            if (success) {
                AlertBox.showInfo("Success", "Pet updated successfully!");
                dialog.close();
                loadPets();
            } else {
                AlertBox.showError("Error", "Failed to update pet.");
            }
        });
        
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        
        dialogContent.getChildren().addAll(
            title,
            new Label("Name:"), nameField,
            new Label("Breed:"), breedField,
            new Label("Age:"), ageField,
            new Label("Status:"), statusCombo,
            new Label("Description:"), descArea,
            buttonBox
        );
        
        Scene dialogScene = new Scene(dialogContent, 350, 450);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    
    private void deletePet(Pet pet) {
        boolean confirm = AlertBox.showConfirmation(
            "Delete Pet",
            "Are you sure you want to delete " + pet.getName() + "?\nThis action cannot be undone."
        );
        
        if (confirm) {
            try {
                boolean success = petService.deletePet(pet.getId());
                if (success) {
                    AlertBox.showInfo("Success", "Pet deleted successfully!");
                    loadData();
                } else {
                    AlertBox.showError("Error", "Failed to delete pet. It may have active adoption requests.");
                }
            } catch (Exception e) {
                AlertBox.showError("Error", "An error occurred while deleting pet: " + e.getMessage());
            }
        }
    }
    
    private void viewAdoptionDetails(Adoption adoption) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Adoption Request Details");
        info.setHeaderText("Request #" + adoption.getId());
        info.initOwner(primaryStage);
        
        StringBuilder content = new StringBuilder();
        
        if (adoption.getPet() != null) {
            content.append("Pet Information:\n")
                  .append("• Name: ").append(adoption.getPet().getName()).append("\n")
                  .append("• Type: ").append(adoption.getPet().getType()).append("\n")
                  .append("• Breed: ").append(adoption.getPet().getBreed()).append("\n\n");
        }
        
        if (adoption.getAdopter() != null) {
            content.append("Adopter Information:\n")
                  .append("• Name: ").append(adoption.getAdopter().getFullName()).append("\n")
                  .append("• Email: ").append(adoption.getAdopter().getEmail()).append("\n")
                  .append("• Phone: ").append(adoption.getAdopter().getPhone() != null ? adoption.getAdopter().getPhone() : "N/A").append("\n\n");
        }
        
        content.append("Request Details:\n")
              .append("• Date: ").append(adoption.getAdoptionDate()).append("\n")
              .append("• Status: ").append(adoption.getStatus()).append("\n")
              .append("• Notes: ").append(adoption.getNotes() != null ? adoption.getNotes() : "None").append("\n");
        
        info.setContentText(content.toString());
        info.showAndWait();
    }
    
    private void processAdoption(Adoption adoption, boolean approve) {
        String action = approve ? "approve" : "reject";
        String petName = adoption.getPet() != null ? adoption.getPet().getName() : "Unknown Pet";
        String adopterName = adoption.getAdopter() != null ? adoption.getAdopter().getFullName() : "Unknown Adopter";
        
        String message = String.format(
            "Are you sure you want to %s the adoption request for %s by %s?",
            action, petName, adopterName
        );
        
        boolean confirm = AlertBox.showConfirmation("Confirm " + action, message);
        
        if (confirm) {
            try {
                String result = adoptionService.processAdoption(adoption.getId(), approve);
                AlertBox.showInfo("Result", result);
                loadData();
            } catch (Exception e) {
                AlertBox.showError("Error", "Failed to process adoption: " + e.getMessage());
            }
        }
    }
    
    private void generatePetsReport() {
        try {
            List<Pet> pets = petService.getAllPets();
            StringBuilder report = new StringBuilder();
            report.append("=".repeat(50)).append("\n");
            report.append("PETS REPORT\n");
            report.append("=".repeat(50)).append("\n");
            report.append("Generated on: ").append(LocalDate.now()).append("\n\n");
            
            if (pets != null && !pets.isEmpty()) {
                report.append("SUMMARY\n");
                report.append("-".repeat(30)).append("\n");
                report.append("Total Pets: ").append(pets.size()).append("\n");
                report.append("Available: ").append(petService.getAvailablePetsCount()).append("\n");
                report.append("Adopted: ").append(petService.getAdoptedPetsCount()).append("\n");
                report.append("Pending: ").append(petService.getPendingPetsCount()).append("\n\n");
                
                report.append("DETAILED LIST\n");
                report.append("-".repeat(30)).append("\n");
                for (Pet pet : pets) {
                    report.append(String.format("- ID: %d | Name: %s | Type: %s | Breed: %s | Age: %d | Status: %s\n",
                        pet.getId(),
                        pet.getName(),
                        pet.getType(),
                        pet.getBreed(),
                        pet.getAge(),
                        pet.getStatus()));
                }
            } else {
                report.append("No pets found in the system.\n");
            }
            
            report.append("\n").append("=".repeat(50)).append("\n");
            report.append("End of Report\n");
            
            showReport("Pets Report", report.toString());
        } catch (Exception e) {
            AlertBox.showError("Error", "Failed to generate pets report: " + e.getMessage());
        }
    }
    
    private void generateAdoptionsReport() {
        try {
            List<Adoption> adoptions = adoptionService.getAllAdoptions();
            StringBuilder report = new StringBuilder();
            report.append("=".repeat(50)).append("\n");
            report.append("ADOPTIONS REPORT\n");
            report.append("=".repeat(50)).append("\n");
            report.append("Generated on: ").append(LocalDate.now()).append("\n\n");
            
            if (adoptions != null && !adoptions.isEmpty()) {
                report.append("Total Adoptions: ").append(adoptions.size()).append("\n\n");
                
                report.append("ADOPTION REQUESTS\n");
                report.append("-".repeat(30)).append("\n");
                for (Adoption adoption : adoptions) {
                    String petName = adoption.getPet() != null ? adoption.getPet().getName() : "Unknown";
                    String adopterName = adoption.getAdopter() != null ? adoption.getAdopter().getFullName() : "Unknown";
                    
                    report.append(String.format("- Request #%d: %s by %s | Status: %s | Date: %s\n",
                        adoption.getId(),
                        petName,
                        adopterName,
                        adoption.getStatus(),
                        adoption.getAdoptionDate()));
                }
            } else {
                report.append("No adoption records found.\n");
            }
            
            report.append("\n").append("=".repeat(50)).append("\n");
            report.append("End of Report\n");
            
            showReport("Adoptions Report", report.toString());
        } catch (Exception e) {
            AlertBox.showError("Error", "Failed to generate adoptions report: " + e.getMessage());
        }
    }
    
    private void generateUsersReport() {
        AlertBox.showInfo("Info", "Users report functionality requires UserService implementation.\nThis feature will be available in the next update.");
    }
    
    private void showReport(String title, String content) {
        Stage reportStage = new Stage();
        reportStage.setTitle(title);
        reportStage.initOwner(primaryStage);
        
        VBox reportContent = new VBox(20);
        reportContent.setPadding(new Insets(30));
        reportContent.setStyle("-fx-background-color: #f8fafc;");
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        titleLabel.setTextFill(Color.web("#1e293b"));
        
        TextArea reportArea = new TextArea(content);
        reportArea.setEditable(false);
        reportArea.setPrefSize(600, 400);
        reportArea.setWrapText(true);
        reportArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button printBtn = createStyledButton("Print Report", "#4f46e5");
        printBtn.setOnAction(e -> {
            // Basic print functionality - in real app, use JavaFX printing API
            System.out.println(content);
            AlertBox.showInfo("Info", "Report sent to console for printing.");
        });
        
        Button closeBtn = createStyledButton("Close", "#64748b");
        closeBtn.setOnAction(e -> reportStage.close());
        
        buttonBox.getChildren().addAll(printBtn, closeBtn);
        
        reportContent.getChildren().addAll(titleLabel, reportArea, buttonBox);
        
        Scene reportScene = new Scene(reportContent, 650, 500);
        reportStage.setScene(reportScene);
        reportStage.show();
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
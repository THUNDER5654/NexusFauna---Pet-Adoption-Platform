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
        scene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
    }
    
    private HBox createMenuBar() {
        HBox menuBar = new HBox(20);
        menuBar.getStyleClass().add("dashboard-header");
        menuBar.setAlignment(Pos.CENTER_LEFT);
        
        // App name
        Label appName = new Label("NexusFauna - Admin Panel");
        appName.getStyleClass().add("dashboard-title");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // User info
        Label userInfo = new Label("Welcome, " + Session.getUserFullName());
        userInfo.setTextFill(Color.WHITE);
        userInfo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        // Logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("button-danger");
        logoutBtn.setOnAction(e -> logout());
        
        menuBar.getChildren().addAll(appName, spacer, userInfo, logoutBtn);
        return menuBar;
    }
    
    private VBox createDashboardContent() {
        VBox dashboard = new VBox(30);
        dashboard.getStyleClass().add("dashboard-content");
        
        // Title
        Label title = new Label("Dashboard Overview");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));
        
        // Stats cards
        HBox statsRow = new HBox(20);
        statsRow.setAlignment(Pos.CENTER);
        
        totalPetsLabel = new Label("0");
        totalPetsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        
        availablePetsLabel = new Label("0");
        availablePetsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        
        pendingAdoptionsLabel = new Label("0");
        pendingAdoptionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        
        adoptedPetsLabel = new Label("0");
        adoptedPetsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        
        VBox totalCard = createStatCard("Total Pets", totalPetsLabel, "#4f46e5");
        VBox availableCard = createStatCard("Available Pets", availablePetsLabel, "#10b981");
        VBox pendingCard = createStatCard("Pending Requests", pendingAdoptionsLabel, "#f59e0b");
        VBox adoptedCard = createStatCard("Adopted Pets", adoptedPetsLabel, "#ef4444");
        
        statsRow.getChildren().addAll(totalCard, availableCard, pendingCard, adoptedCard);
        
        // Quick actions
        Label actionsLabel = new Label("Quick Actions");
        actionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button addPetBtn = new Button("Add New Pet");
        addPetBtn.getStyleClass().add("button-success");
        addPetBtn.setOnAction(e -> showAddPetDialog());
        
        Button viewRequestsBtn = new Button("View Adoption Requests");
        viewRequestsBtn.getStyleClass().add("button-primary");
        viewRequestsBtn.setOnAction(e -> tabPane.getSelectionModel().select(2));
        
        Button refreshBtn = new Button("Refresh Dashboard");
        refreshBtn.setOnAction(e -> loadData());
        
        actionButtons.getChildren().addAll(addPetBtn, viewRequestsBtn, refreshBtn);
        
        dashboard.getChildren().addAll(title, statsRow, actionsLabel, actionButtons);
        return dashboard;
    }
    
    private VBox createStatCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + color + "; " +
                     "-fx-background-radius: 12px;");
        card.setPrefWidth(200);
        card.setPrefHeight(120);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.WHITE);
        
        valueLabel.setTextFill(Color.WHITE);
        
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
        
        Button addBtn = new Button("+ Add New Pet");
        addBtn.getStyleClass().add("button-success");
        addBtn.setOnAction(e -> showAddPetDialog());
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadPets());
        
        header.getChildren().addAll(title, spacer, addBtn, refreshBtn);
        
        // Pets table
        petsTable = new TableView<>();
        setupPetsTable();
        
        petsContent.getChildren().addAll(header, petsTable);
        return petsContent;
    }
    
    private void setupPetsTable() {
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
                editBtn.getStyleClass().add("button-secondary");
                deleteBtn.getStyleClass().add("button-danger");
                
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
        
        petsTable.getColumns().addAll(idCol, nameCol, typeCol, breedCol, ageCol, statusCol, actionsCol);
        petsTable.setPrefHeight(500);
    }
    
    private VBox createAdoptionsContent() {
        VBox adoptionsContent = new VBox(20);
        adoptionsContent.setPadding(new Insets(20));
        
        Label title = new Label("Pending Adoption Requests");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        adoptionsTable = new TableView<>();
        setupAdoptionsTable();
        
        Button refreshBtn = new Button("Refresh Requests");
        refreshBtn.setOnAction(e -> loadAdoptions());
        
        adoptionsContent.getChildren().addAll(title, adoptionsTable, refreshBtn);
        return adoptionsContent;
    }
    
    private void setupAdoptionsTable() {
        // ID column
        TableColumn<Adoption, Integer> idCol = new TableColumn<>("Request ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(100);
        
        // Pet column
        TableColumn<Adoption, String> petCol = new TableColumn<>("Pet");
        petCol.setCellValueFactory(cellData -> 
            cellData.getValue().getPet().nameProperty());
        petCol.setPrefWidth(150);
        
        // Adopter column
        TableColumn<Adoption, String> adopterCol = new TableColumn<>("Adopter");
        adopterCol.setCellValueFactory(cellData -> 
            cellData.getValue().getAdopter().fullNameProperty());
        adopterCol.setPrefWidth(150);
        
        // Date column
        TableColumn<Adoption, String> dateCol = new TableColumn<>("Request Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("adoptionDate"));
        dateCol.setPrefWidth(120);
        
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
                viewBtn.getStyleClass().add("button-secondary");
                approveBtn.getStyleClass().add("button-success");
                rejectBtn.getStyleClass().add("button-danger");
                
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
        
        adoptionsTable.getColumns().addAll(idCol, petCol, adopterCol, dateCol, notesCol, actionsCol);
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
        
        Button petsReportBtn = new Button("Generate Pets Report");
        petsReportBtn.getStyleClass().add("button-primary");
        petsReportBtn.setPrefWidth(300);
        petsReportBtn.setOnAction(e -> generatePetsReport());
        
        Button adoptionsReportBtn = new Button("Generate Adoptions Report");
        adoptionsReportBtn.getStyleClass().add("button-primary");
        adoptionsReportBtn.setPrefWidth(300);
        adoptionsReportBtn.setOnAction(e -> generateAdoptionsReport());
        
        Button usersReportBtn = new Button("Generate Users Report");
        usersReportBtn.getStyleClass().add("button-primary");
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
        totalPetsLabel.setText(String.valueOf(petService.getTotalPetsCount()));
        availablePetsLabel.setText(String.valueOf(petService.getAvailablePetsCount()));
        adoptedPetsLabel.setText(String.valueOf(petService.getAdoptedPetsCount()));
        
        List<Adoption> pending = adoptionService.getPendingAdoptions();
        pendingAdoptionsLabel.setText(String.valueOf(pending.size()));
    }
    
    private void loadPets() {
        List<Pet> pets = petService.getAllPets();
        petList = FXCollections.observableArrayList(pets);
        petsTable.setItems(petList);
    }
    
    private void loadAdoptions() {
        List<Adoption> adoptions = adoptionService.getPendingAdoptions();
        adoptionList = FXCollections.observableArrayList(adoptions);
        adoptionsTable.setItems(adoptionList);
    }
    
    private void showAddPetDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Pet");
        
        VBox dialogContent = new VBox(15);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setAlignment(Pos.CENTER);
        
        Label title = new Label("Add New Pet");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        // Form fields
        TextField nameField = new TextField();
        nameField.setPromptText("Pet Name");
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Dog", "Cat", "Bird", "Rabbit", "Fish", "Other");
        typeCombo.setPromptText("Select Type");
        
        TextField breedField = new TextField();
        breedField.setPromptText("Breed");
        
        TextField ageField = new TextField();
        ageField.setPromptText("Age (years)");
        
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female", "Unknown");
        genderCombo.setPromptText("Select Gender");
        
        TextField healthField = new TextField();
        healthField.setPromptText("Health Status");
        healthField.setText("Good");
        
        CheckBox vaccinatedCheck = new CheckBox("Vaccinated");
        
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefHeight(100);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button saveBtn = new Button("Save");
        saveBtn.getStyleClass().add("button-success");
        saveBtn.setOnAction(e -> {
            String error = Validator.validatePet(
                nameField.getText(), 
                typeCombo.getValue(),
                ageField.getText(),
                genderCombo.getValue(),
                healthField.getText()
            );
            
            if (error != null) {
                AlertBox.showError("Validation Error", error);
                return;
            }
            
            Pet pet = new Pet(
                nameField.getText(),
                typeCombo.getValue(),
                breedField.getText(),
                Integer.parseInt(ageField.getText()),
                genderCombo.getValue(),
                healthField.getText(),
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
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        
        dialogContent.getChildren().addAll(
            title,
            nameField, typeCombo, breedField, ageField,
            genderCombo, healthField, vaccinatedCheck,
            descArea, buttonBox
        );
        
        Scene dialogScene = new Scene(dialogContent, 400, 600);
        dialogScene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    
    private void editPet(Pet pet) {
        // Similar to add dialog but pre-filled
        AlertBox.showInfo("Coming Soon", "Edit functionality will be available in the next update.");
    }
    
    private void deletePet(Pet pet) {
        boolean confirm = AlertBox.showConfirmation(
            "Delete Pet",
            "Are you sure you want to delete " + pet.getName() + "?\nThis action cannot be undone."
        );
        
        if (confirm) {
            boolean success = petService.deletePet(pet.getId());
            if (success) {
                AlertBox.showInfo("Success", "Pet deleted successfully!");
                loadData();
            } else {
                AlertBox.showError("Error", "Failed to delete pet. It may have active adoption requests.");
            }
        }
    }
    
    private void viewAdoptionDetails(Adoption adoption) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Adoption Request Details");
        info.setHeaderText("Request #" + adoption.getId());
        
        String content = String.format("""
            Pet Information:
            • Name: %s
            • Type: %s
            • Breed: %s
            
            Adopter Information:
            • Name: %s
            • Email: %s
            • Phone: %s
            
            Request Details:
            • Date: %s
            • Status: %s
            • Notes: %s
            """,
            adoption.getPet().getName(),
            adoption.getPet().getType(),
            adoption.getPet().getBreed(),
            adoption.getAdopter().getFullName(),
            adoption.getAdopter().getEmail(),
            adoption.getAdopter().getPhone(),
            adoption.getAdoptionDate(),
            adoption.getStatus(),
            adoption.getNotes()
        );
        
        info.setContentText(content);
        info.showAndWait();
    }
    
    private void processAdoption(Adoption adoption, boolean approve) {
        String action = approve ? "approve" : "reject";
        String message = String.format(
            "Are you sure you want to %s the adoption request for %s by %s?\n\nPet: %s (%s)\nAdopter: %s",
            action, adoption.getPet().getName(), adoption.getAdopter().getFullName(),
            adoption.getPet().getName(), adoption.getPet().getBreed(),
            adoption.getAdopter().getFullName()
        );
        
        boolean confirm = AlertBox.showConfirmation("Confirm " + action, message);
        
        if (confirm) {
            String result = adoptionService.processAdoption(adoption.getId(), approve);
            AlertBox.showInfo("Result", result);
            loadData();
        }
    }
    
    private void generatePetsReport() {
        List<Pet> pets = petService.getAllPets();
        StringBuilder report = new StringBuilder();
        report.append("PETS REPORT\n");
        report.append("Generated on: ").append(LocalDate.now()).append("\n\n");
        report.append("Total Pets: ").append(pets.size()).append("\n");
        report.append("Available: ").append(petService.getAvailablePetsCount()).append("\n");
        report.append("Adopted: ").append(petService.getAdoptedPetsCount()).append("\n");
        report.append("Pending: ").append(petService.getPendingPetsCount()).append("\n\n");
        
        report.append("Pet Details:\n");
        for (Pet pet : pets) {
            report.append(String.format("- %s (%s), %d years, Status: %s\n",
                pet.getName(), pet.getType(), pet.getAge(), pet.getStatus()));
        }
        
        showReport("Pets Report", report.toString());
    }
    
    private void generateAdoptionsReport() {
        List<Adoption> adoptions = adoptionService.getAllAdoptions();
        StringBuilder report = new StringBuilder();
        report.append("ADOPTIONS REPORT\n");
        report.append("Generated on: ").append(LocalDate.now()).append("\n\n");
        report.append("Total Adoptions: ").append(adoptions.size()).append("\n\n");
        
        report.append("Adoption Details:\n");
        for (Adoption adoption : adoptions) {
            report.append(String.format("- Request #%d: %s by %s, Status: %s, Date: %s\n",
                adoption.getId(),
                adoption.getPet().getName(),
                adoption.getAdopter().getFullName(),
                adoption.getStatus(),
                adoption.getAdoptionDate()));
        }
        
        showReport("Adoptions Report", report.toString());
    }
    
    private void generateUsersReport() {
        // This would require a UserService method to get all users
        AlertBox.showInfo("Coming Soon", "Users report functionality will be available in the next update.");
    }
    
    private void showReport(String title, String content) {
        Stage reportStage = new Stage();
        reportStage.setTitle(title);
        
        VBox reportContent = new VBox(20);
        reportContent.setPadding(new Insets(30));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        TextArea reportArea = new TextArea(content);
        reportArea.setEditable(false);
        reportArea.setPrefSize(600, 400);
        reportArea.setWrapText(true);
        
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> reportStage.close());
        
        reportContent.getChildren().addAll(titleLabel, reportArea, closeBtn);
        
        Scene reportScene = new Scene(reportContent, 650, 500);
        reportScene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
        reportStage.setScene(reportScene);
        reportStage.show();
    }
    
    private void logout() {
        Session.clear();
        LoginController loginController = new LoginController(primaryStage);
        primaryStage.setScene(loginController.getScene());
        primaryStage.setMaximized(false);
        primaryStage.centerOnScreen();
    }
    
    public Scene getScene() {
        return scene;
    }
}
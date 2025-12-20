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
    
    public AdopterDashboardController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.petService = new PetService();
        this.adoptionService = new AdoptionService();
        buildUI();
        loadData();
    }
    
    private void buildUI() {
        BorderPane mainPane = new BorderPane();
        mainPane.getStyleClass().add("root");
        
        // Top menu bar
        HBox menuBar = createMenuBar();
        mainPane.setTop(menuBar);
        
        // Center - TabPane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Available Pets Tab
        Tab petsTab = new Tab("Available Pets");
        petsTab.setContent(createAvailablePetsContent());
        
        // My Adoptions Tab
        Tab adoptionsTab = new Tab("My Adoption Requests");
        adoptionsTab.setContent(createMyAdoptionsContent());
        
        // Profile Tab
        Tab profileTab = new Tab("My Profile");
        profileTab.setContent(createProfileContent());
        
        tabPane.getTabs().addAll(petsTab, adoptionsTab, profileTab);
        mainPane.setCenter(tabPane);
        
        scene = new Scene(mainPane, 1400, 850);
        scene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
    }
    
    private HBox createMenuBar() {
        HBox menuBar = new HBox(20);
        menuBar.getStyleClass().add("dashboard-header");
        menuBar.setAlignment(Pos.CENTER_LEFT);
        
        Label appName = new Label("NexusFauna - Adopter Panel");
        appName.getStyleClass().add("dashboard-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label userInfo = new Label("Welcome, " + Session.getUserFullName());
        userInfo.setTextFill(Color.WHITE);
        userInfo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.getStyleClass().add("button-danger");
        logoutBtn.setOnAction(e -> logout());
        
        menuBar.getChildren().addAll(appName, spacer, userInfo, logoutBtn);
        return menuBar;
    }
    
    private VBox createAvailablePetsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("Available Pets for Adoption");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshBtn = new Button("Refresh List");
        refreshBtn.setOnAction(e -> loadAvailablePets());
        
        header.getChildren().addAll(title, spacer, refreshBtn);
        
        // Pets table
        availablePetsTable = new TableView<>();
        setupAvailablePetsTable();
        
        content.getChildren().addAll(header, availablePetsTable);
        return content;
    }
    
    private void setupAvailablePetsTable() {
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
        
        // Gender column
        TableColumn<Pet, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setPrefWidth(100);
        
        // Health column
        TableColumn<Pet, String> healthCol = new TableColumn<>("Health Status");
        healthCol.setCellValueFactory(new PropertyValueFactory<>("healthStatus"));
        healthCol.setPrefWidth(120);
        
        // Vaccinated column
        TableColumn<Pet, String> vaccinatedCol = new TableColumn<>("Vaccinated");
        vaccinatedCol.setCellValueFactory(cellData -> 
            cellData.getValue().isVaccinated() ? "Yes" : "No");
        vaccinatedCol.setPrefWidth(100);
        
        // Description column
        TableColumn<Pet, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(200);
        
        // Adopt button column
        TableColumn<Pet, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<Pet, Void>() {
            private final Button adoptBtn = new Button("Adopt");
            
            {
                adoptBtn.getStyleClass().add("button-success");
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
            healthCol, vaccinatedCol, descCol, actionCol
        );
        availablePetsTable.setPrefHeight(500);
    }
    
    private VBox createMyAdoptionsContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        Label title = new Label("My Adoption Requests");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        myAdoptionsTable = new TableView<>();
        setupMyAdoptionsTable();
        
        Button refreshBtn = new Button("Refresh My Requests");
        refreshBtn.setOnAction(e -> loadMyAdoptions());
        
        content.getChildren().addAll(title, myAdoptionsTable, refreshBtn);
        return content;
    }
    
    private void setupMyAdoptionsTable() {
        // Pet column
        TableColumn<Adoption, String> petCol = new TableColumn<>("Pet");
        petCol.setCellValueFactory(cellData -> 
            cellData.getValue().getPet().nameProperty());
        petCol.setPrefWidth(150);
        
        // Type column
        TableColumn<Adoption, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> 
            cellData.getValue().getPet().typeProperty());
        typeCol.setPrefWidth(100);
        
        // Date column
        TableColumn<Adoption, String> dateCol = new TableColumn<>("Request Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("adoptionDate"));
        dateCol.setPrefWidth(120);
        
        // Status column
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
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            break;
                        case "Approved":
                            setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                            break;
                        case "Rejected":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });
        
        // Notes column
        TableColumn<Adoption, String> notesCol = new TableColumn<>("My Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesCol.setPrefWidth(200);
        
        myAdoptionsTable.getColumns().addAll(petCol, typeCol, dateCol, statusCol, notesCol);
        myAdoptionsTable.setPrefHeight(400);
    }
    
    private VBox createProfileContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);
        
        Label title = new Label("My Profile");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Profile info card
        VBox profileCard = new VBox(20);
        profileCard.getStyleClass().add("card");
        profileCard.setAlignment(Pos.CENTER);
        profileCard.setPrefWidth(500);
        profileCard.setPadding(new Insets(30));
        
        Label nameLabel = new Label("Name: " + Session.getUserFullName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        Label usernameLabel = new Label("Username: " + Session.getUsername());
        usernameLabel.setFont(Font.font("Arial", 14));
        
        Label emailLabel = new Label("Email: " + Session.getUserEmail());
        emailLabel.setFont(Font.font("Arial", 14));
        
        Label roleLabel = new Label("Role: Adopter");
        roleLabel.setFont(Font.font("Arial", 14));
        
        profileCard.getChildren().addAll(title, nameLabel, usernameLabel, emailLabel, roleLabel);
        
        content.getChildren().add(profileCard);
        return content;
    }
    
    private void loadData() {
        loadAvailablePets();
        loadMyAdoptions();
    }
    
    private void loadAvailablePets() {
        List<Pet> pets = petService.getAvailablePets();
        availablePetsList = FXCollections.observableArrayList(pets);
        availablePetsTable.setItems(availablePetsList);
    }
    
    private void loadMyAdoptions() {
        List<Adoption> adoptions = adoptionService.getAdopterAdoptions(Session.getUserId());
        myAdoptionsList = FXCollections.observableArrayList(adoptions);
        myAdoptionsTable.setItems(myAdoptionsList);
    }
    
    private void adoptPet(Pet pet) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Adopt " + pet.getName());
        
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setAlignment(Pos.CENTER);
        
        Label title = new Label("Adopt " + pet.getName());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        // Pet info
        VBox petInfo = new VBox(10);
        petInfo.setAlignment(Pos.CENTER_LEFT);
        
        Label typeLabel = new Label("Type: " + pet.getType());
        Label breedLabel = new Label("Breed: " + pet.getBreed());
        Label ageLabel = new Label("Age: " + pet.getAge() + " years");
        Label genderLabel = new Label("Gender: " + pet.getGender());
        Label healthLabel = new Label("Health: " + pet.getHealthStatus());
        Label vaccinatedLabel = new Label("Vaccinated: " + (pet.isVaccinated() ? "Yes" : "No"));
        
        petInfo.getChildren().addAll(typeLabel, breedLabel, ageLabel, genderLabel, healthLabel, vaccinatedLabel);
        
        // Notes field
        Label notesLabel = new Label("Why do you want to adopt this pet?");
        notesLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Tell us about your home, experience with pets, why you want to adopt, etc.");
        notesArea.setPrefHeight(100);
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button submitBtn = new Button("Submit Adoption Request");
        submitBtn.getStyleClass().add("button-success");
        submitBtn.setOnAction(e -> {
            if (!Validator.isNotEmpty(notesArea.getText())) {
                AlertBox.showError("Error", "Please provide adoption notes");
                return;
            }
            
            String result = adoptionService.requestAdoption(
                pet.getId(),
                Session.getUserId(),
                notesArea.getText()
            );
            
            AlertBox.showInfo("Adoption Request", result);
            dialog.close();
            loadData(); // Refresh both tables
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(submitBtn, cancelBtn);
        
        dialogContent.getChildren().addAll(title, petInfo, notesLabel, notesArea, buttonBox);
        
        Scene dialogScene = new Scene(dialogContent, 500, 450);
        dialogScene.getStylesheets().add(getClass().getResource("/styles/Styles.css").toExternalForm());
        dialog.setScene(dialogScene);
        dialog.showAndWait();
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
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
import models.Pet;
import services.PetService;
import util.AlertBox;
import util.Validator;
import java.util.List;

public class PetManagementController {
    private Stage primaryStage;
    private Scene scene;
    private PetService petService;
    
    // UI Components
    private TableView<Pet> petsTable;
    private ObservableList<Pet> petList;
    private TextField searchField;
    
    public PetManagementController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.petService = new PetService();
        buildUI();
        loadPets();
    }
    
    private void buildUI() {
        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(20));
        mainPane.setStyle("-fx-background-color: #f8fafc;");
        
        // Header
        VBox header = new VBox(15);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Label title = new Label("Pet Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));
        
        // Search and filter bar
        HBox searchBar = new HBox(15);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Search pets by name, type, or breed...");
        searchField.setPrefWidth(300);
        
        Button searchBtn = new Button("Search");
        searchBtn.getStyleClass().add("button-primary");
        searchBtn.setOnAction(e -> searchPets());
        
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            loadPets();
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button addBtn = new Button("+ Add New Pet");
        addBtn.getStyleClass().add("button-success");
        addBtn.setOnAction(e -> showAddPetDialog());
        
        searchBar.getChildren().addAll(searchField, searchBtn, clearBtn, spacer, addBtn);
        
        header.getChildren().addAll(title, searchBar);
        mainPane.setTop(header);
        
        // Pets table
        petsTable = createPetsTable();
        mainPane.setCenter(petsTable);
        
        // Footer with statistics
        HBox footer = new HBox(20);
        footer.setPadding(new Insets(20, 0, 0, 0));
        footer.setAlignment(Pos.CENTER);
        
        Label statsLabel = new Label("Total Pets: " + petService.getTotalPetsCount() + 
                                   " | Available: " + petService.getAvailablePetsCount() +
                                   " | Adopted: " + petService.getAdoptedPetsCount());
        statsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        footer.getChildren().add(statsLabel);
        mainPane.setBottom(footer);
        
        scene = new Scene(mainPane, 1200, 700);
    }
    
    private TableView<Pet> createPetsTable() {
        TableView<Pet> table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        
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
        
        // Gender column
        TableColumn<Pet, String> genderCol = new TableColumn<>("Gender");
        genderCol.setCellValueFactory(new PropertyValueFactory<>("gender"));
        genderCol.setPrefWidth(100);
        
        // Status column with color coding
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
        
        // Added date column
        TableColumn<Pet, String> dateCol = new TableColumn<>("Added Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("addedDate"));
        dateCol.setPrefWidth(120);
        
        // Actions column
        TableColumn<Pet, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(col -> new TableCell<Pet, Void>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, viewBtn, editBtn, deleteBtn);
            
            {
                viewBtn.getStyleClass().add("button-secondary");
                editBtn.getStyleClass().add("button-secondary");
                deleteBtn.getStyleClass().add("button-danger");
                
                viewBtn.setOnAction(e -> {
                    Pet pet = getTableView().getItems().get(getIndex());
                    viewPetDetails(pet);
                });
                
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
        
        table.getColumns().addAll(idCol, nameCol, typeCol, breedCol, ageCol, 
                                 genderCol, statusCol, dateCol, actionsCol);
        table.setPrefHeight(500);
        
        return table;
    }
    
    private void loadPets() {
        List<Pet> pets = petService.getAllPets();
        petList = FXCollections.observableArrayList(pets);
        petsTable.setItems(petList);
    }
    
    private void searchPets() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            loadPets();
            return;
        }
        
        List<Pet> filteredPets = petList.filtered(pet -> 
            pet.getName().toLowerCase().contains(query) ||
            pet.getType().toLowerCase().contains(query) ||
            (pet.getBreed() != null && pet.getBreed().toLowerCase().contains(query)) ||
            pet.getStatus().toLowerCase().contains(query)
        );
        
        petsTable.setItems(FXCollections.observableArrayList(filteredPets));
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
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(20, 0, 20, 0));
        
        // Row 0
        Label nameLabel = new Label("Pet Name:");
        TextField nameField = new TextField();
        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);
        
        // Row 1
        Label typeLabel = new Label("Type:");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Dog", "Cat", "Bird", "Rabbit", "Fish", "Other");
        form.add(typeLabel, 0, 1);
        form.add(typeCombo, 1, 1);
        
        // Row 2
        Label breedLabel = new Label("Breed:");
        TextField breedField = new TextField();
        form.add(breedLabel, 0, 2);
        form.add(breedField, 1, 2);
        
        // Row 3
        Label ageLabel = new Label("Age (years):");
        TextField ageField = new TextField();
        form.add(ageLabel, 0, 3);
        form.add(ageField, 1, 3);
        
        // Row 4
        Label genderLabel = new Label("Gender:");
        ComboBox<String> genderCombo = new ComboBox<>();
        genderCombo.getItems().addAll("Male", "Female", "Unknown");
        form.add(genderLabel, 0, 4);
        form.add(genderCombo, 1, 4);
        
        // Row 5
        Label healthLabel = new Label("Health Status:");
        TextField healthField = new TextField();
        healthField.setText("Good");
        form.add(healthLabel, 0, 5);
        form.add(healthField, 1, 5);
        
        // Row 6
        Label vaccinatedLabel = new Label("Vaccinated:");
        CheckBox vaccinatedCheck = new CheckBox("Yes");
        form.add(vaccinatedLabel, 0, 6);
        form.add(vaccinatedCheck, 1, 6);
        
        // Row 7
        Label descLabel = new Label("Description:");
        TextArea descArea = new TextArea();
        descArea.setPrefHeight(80);
        form.add(descLabel, 0, 7);
        form.add(descArea, 1, 7);
        
        // Buttons
        HBox buttonBox = new HBox(15);
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
                loadPets();
            } else {
                AlertBox.showError("Error", "Failed to add pet");
            }
        });
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> dialog.close());
        
        buttonBox.getChildren().addAll(saveBtn, cancelBtn);
        
        dialogContent.getChildren().addAll(title, form, buttonBox);
        
        Scene dialogScene = new Scene(dialogContent, 450, 600);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }
    
    private void viewPetDetails(Pet pet) {
        Stage detailStage = new Stage();
        detailStage.setTitle("Pet Details: " + pet.getName());
        
        VBox detailContent = new VBox(20);
        detailContent.setPadding(new Insets(30));
        detailContent.setAlignment(Pos.CENTER);
        
        Label title = new Label(pet.getName());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Pet details
        VBox details = new VBox(10);
        details.setAlignment(Pos.CENTER_LEFT);
        
        Label typeLabel = new Label("Type: " + pet.getType());
        Label breedLabel = new Label("Breed: " + pet.getBreed());
        Label ageLabel = new Label("Age: " + pet.getAge() + " years");
        Label genderLabel = new Label("Gender: " + pet.getGender());
        Label healthLabel = new Label("Health: " + pet.getHealthStatus());
        Label vaccinatedLabel = new Label("Vaccinated: " + (pet.isVaccinated() ? "Yes" : "No"));
        Label statusLabel = new Label("Status: " + pet.getStatus());
        Label dateLabel = new Label("Added: " + pet.getAddedDate());
        
        // Description
        Label descLabel = new Label("Description:");
        descLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        TextArea descArea = new TextArea(pet.getDescription());
        descArea.setEditable(false);
        descArea.setPrefHeight(100);
        
        details.getChildren().addAll(
            typeLabel, breedLabel, ageLabel, genderLabel,
            healthLabel, vaccinatedLabel, statusLabel, dateLabel,
            descLabel, descArea
        );
        
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> detailStage.close());
        
        detailContent.getChildren().addAll(title, details, closeBtn);
        
        Scene detailScene = new Scene(detailContent, 500, 500);
        detailStage.setScene(detailScene);
        detailStage.show();
    }
    
    private void editPet(Pet pet) {
        AlertBox.showInfo("Edit Pet", "Edit functionality will be available in the next update.");
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
                loadPets();
            } else {
                AlertBox.showError("Error", "Failed to delete pet. It may have active adoption requests.");
            }
        }
    }
    
    public Scene getScene() {
        return scene;
    }
}
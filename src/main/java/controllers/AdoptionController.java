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
import services.AdoptionService;
import util.AlertBox;
import util.Session;
import java.util.List;

public class AdoptionController {
    private Stage primaryStage;
    private Scene scene;
    private AdoptionService adoptionService;
    
    // UI Components
    private TableView<Adoption> adoptionsTable;
    private ObservableList<Adoption> adoptionList;
    private ComboBox<String> statusFilter;
    
    public AdoptionController(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.adoptionService = new AdoptionService();
        buildUI();
        loadAdoptions();
    }
    
    private void buildUI() {
        BorderPane mainPane = new BorderPane();
        mainPane.setPadding(new Insets(20));
        mainPane.setStyle("-fx-background-color: #f8fafc;");
        
        // Header
        VBox header = new VBox(15);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Label title = new Label("Adoption Management");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#1e293b"));
        
        // Filter and action bar
        HBox actionBar = new HBox(15);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filter by Status:");
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Pending", "Approved", "Rejected");
        statusFilter.setValue("All");
        statusFilter.setOnAction(e -> filterAdoptions());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> loadAdoptions());
        
        actionBar.getChildren().addAll(filterLabel, statusFilter, spacer, refreshBtn);
        
        header.getChildren().addAll(title, actionBar);
        mainPane.setTop(header);
        
        // Adoptions table
        adoptionsTable = createAdoptionsTable();
        mainPane.setCenter(adoptionsTable);
        
        scene = new Scene(mainPane, 1300, 700);
    }
    
    private TableView<Adoption> createAdoptionsTable() {
        TableView<Adoption> table = new TableView<>();
        table.setStyle("-fx-background-color: white; -fx-background-radius: 8px;");
        
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
        
        // Status column with color coding
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
        TableColumn<Adoption, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesCol.setPrefWidth(200);
        
        // Actions column (only for admins)
        if (Session.isAdmin()) {
            TableColumn<Adoption, Void> actionsCol = new TableColumn<>("Actions");
            actionsCol.setPrefWidth(200);
            actionsCol.setCellFactory(col -> new TableCell<Adoption, Void>() {
                private final Button viewBtn = new Button("View");
                private final Button approveBtn = new Button("Approve");
                private final Button rejectBtn = new Button("Reject");
                private final HBox buttons = new HBox(5, viewBtn, approveBtn, rejectBtn);
                
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
            
            table.getColumns().addAll(idCol, petCol, adopterCol, dateCol, statusCol, notesCol, actionsCol);
        } else {
            table.getColumns().addAll(idCol, petCol, adopterCol, dateCol, statusCol, notesCol);
        }
        
        table.setPrefHeight(500);
        return table;
    }
    
    private void loadAdoptions() {
        if (Session.isAdmin()) {
            List<Adoption> adoptions = adoptionService.getAllAdoptions();
            adoptionList = FXCollections.observableArrayList(adoptions);
        } else {
            List<Adoption> adoptions = adoptionService.getAdopterAdoptions(Session.getUserId());
            adoptionList = FXCollections.observableArrayList(adoptions);
        }
        adoptionsTable.setItems(adoptionList);
    }
    
    private void filterAdoptions() {
        String filter = statusFilter.getValue();
        if (filter.equals("All")) {
            adoptionsTable.setItems(adoptionList);
        } else {
            ObservableList<Adoption> filtered = FXCollections.observableArrayList(
                adoptionList.filtered(adoption -> adoption.getStatus().equals(filter))
            );
            adoptionsTable.setItems(filtered);
        }
    }
    
    private void viewAdoptionDetails(Adoption adoption) {
        Stage detailStage = new Stage();
        detailStage.setTitle("Adoption Request #" + adoption.getId());
        
        VBox detailContent = new VBox(20);
        detailContent.setPadding(new Insets(30));
        detailContent.setAlignment(Pos.CENTER);
        
        Label title = new Label("Adoption Request Details");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Pet section
        VBox petSection = new VBox(10);
        petSection.setStyle("-fx-background-color: #f9fafb; -fx-padding: 15px; -fx-background-radius: 8px;");
        
        Label petTitle = new Label("Pet Information");
        petTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Label petName = new Label("Name: " + adoption.getPet().getName());
        Label petType = new Label("Type: " + adoption.getPet().getType());
        Label petBreed = new Label("Breed: " + adoption.getPet().getBreed());
        
        petSection.getChildren().addAll(petTitle, petName, petType, petBreed);
        
        // Adopter section
        VBox adopterSection = new VBox(10);
        adopterSection.setStyle("-fx-background-color: #f9fafb; -fx-padding: 15px; -fx-background-radius: 8px;");
        
        Label adopterTitle = new Label("Adopter Information");
        adopterTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Label adopterName = new Label("Name: " + adoption.getAdopter().getFullName());
        Label adopterEmail = new Label("Email: " + adoption.getAdopter().getEmail());
        Label adopterPhone = new Label("Phone: " + adoption.getAdopter().getPhone());
        
        adopterSection.getChildren().addAll(adopterTitle, adopterName, adopterEmail, adopterPhone);
        
        // Request details
        VBox requestSection = new VBox(10);
        requestSection.setStyle("-fx-background-color: #f9fafb; -fx-padding: 15px; -fx-background-radius: 8px;");
        
        Label requestTitle = new Label("Request Details");
        requestTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Label requestId = new Label("Request ID: " + adoption.getId());
        Label requestDate = new Label("Date: " + adoption.getAdoptionDate());
        Label requestStatus = new Label("Status: " + adoption.getStatus());
        
        // Status label with color
        switch (adoption.getStatus()) {
            case "Pending":
                requestStatus.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                break;
            case "Approved":
                requestStatus.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                break;
            case "Rejected":
                requestStatus.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                break;
        }
        
        requestSection.getChildren().addAll(requestTitle, requestId, requestDate, requestStatus);
        
        // Notes
        VBox notesSection = new VBox(10);
        notesSection.setStyle("-fx-background-color: #f9fafb; -fx-padding: 15px; -fx-background-radius: 8px;");
        
        Label notesTitle = new Label("Adoption Notes");
        notesTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        TextArea notesArea = new TextArea(adoption.getNotes());
        notesArea.setEditable(false);
        notesArea.setPrefHeight(100);
        notesArea.setWrapText(true);
        
        notesSection.getChildren().addAll(notesTitle, notesArea);
        
        Button closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> detailStage.close());
        
        detailContent.getChildren().addAll(
            title, petSection, adopterSection, requestSection, notesSection, closeBtn
        );
        
        Scene detailScene = new Scene(detailContent, 500, 600);
        detailStage.setScene(detailScene);
        detailStage.show();
    }
    
    private void processAdoption(Adoption adoption, boolean approve) {
        if (!Session.isAdmin()) {
            AlertBox.showError("Permission Denied", "Only administrators can process adoption requests.");
            return;
        }
        
        String action = approve ? "approve" : "reject";
        String message = String.format(
            "Are you sure you want to %s this adoption request?\n\n" +
            "Pet: %s\n" +
            "Adopter: %s\n" +
            "Request Date: %s",
            action,
            adoption.getPet().getName(),
            adoption.getAdopter().getFullName(),
            adoption.getAdoptionDate()
        );
        
        boolean confirm = AlertBox.showConfirmation("Confirm " + action, message);
        
        if (confirm) {
            String result = adoptionService.processAdoption(adoption.getId(), approve);
            AlertBox.showInfo("Adoption Processed", result);
            loadAdoptions();
        }
    }
    
    public Scene getScene() {
        return scene;
    }
}
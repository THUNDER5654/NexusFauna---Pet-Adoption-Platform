package models;

import java.time.LocalDate;
import javafx.beans.property.*;

public class Adoption {
    private final IntegerProperty id;
    private final IntegerProperty petId;
    private final IntegerProperty adopterId;
    private final ObjectProperty<LocalDate> adoptionDate;
    private final StringProperty status;
    private final StringProperty notes;
    private Pet pet;
    private User adopter;
    
    public Adoption() {
        this.id = new SimpleIntegerProperty();
        this.petId = new SimpleIntegerProperty();
        this.adopterId = new SimpleIntegerProperty();
        this.adoptionDate = new SimpleObjectProperty<>(LocalDate.now());
        this.status = new SimpleStringProperty("Pending");
        this.notes = new SimpleStringProperty();
    }
    
    // Getters and Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }
    
    public int getPetId() { return petId.get(); }
    public void setPetId(int petId) { this.petId.set(petId); }
    public IntegerProperty petIdProperty() { return petId; }
    
    public int getAdopterId() { return adopterId.get(); }
    public void setAdopterId(int adopterId) { this.adopterId.set(adopterId); }
    public IntegerProperty adopterIdProperty() { return adopterId; }
    
    public LocalDate getAdoptionDate() { return adoptionDate.get(); }
    public void setAdoptionDate(LocalDate adoptionDate) { this.adoptionDate.set(adoptionDate); }
    public ObjectProperty<LocalDate> adoptionDateProperty() { return adoptionDate; }
    
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }
    
    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }
    
    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }
    
    public User getAdopter() { return adopter; }
    public void setAdopter(User adopter) { this.adopter = adopter; }
    
    @Override
    public String toString() {
        return "Adoption #" + id.get() + " - Status: " + status.get();
    }
}
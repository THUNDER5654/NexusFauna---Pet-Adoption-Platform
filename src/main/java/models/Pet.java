package models;

import java.time.LocalDate;
import javafx.beans.property.*;

public class Pet {
    private final IntegerProperty id;
    private final StringProperty name;
    private final StringProperty type;
    private final StringProperty breed;
    private final IntegerProperty age;
    private final StringProperty gender;
    private final StringProperty healthStatus;
    private final BooleanProperty vaccinated;
    private final StringProperty description;
    private final StringProperty status;
    private final ObjectProperty<LocalDate> addedDate;
    
    public Pet() {
        this.id = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.type = new SimpleStringProperty();
        this.breed = new SimpleStringProperty();
        this.age = new SimpleIntegerProperty();
        this.gender = new SimpleStringProperty();
        this.healthStatus = new SimpleStringProperty();
        this.vaccinated = new SimpleBooleanProperty();
        this.description = new SimpleStringProperty();
        this.status = new SimpleStringProperty("Available");
        this.addedDate = new SimpleObjectProperty<>(LocalDate.now());
    }
    
    public Pet(String name, String type, String breed, int age, String gender, 
               String healthStatus, boolean vaccinated, String description) {
        this();
        this.name.set(name);
        this.type.set(type);
        this.breed.set(breed);
        this.age.set(age);
        this.gender.set(gender);
        this.healthStatus.set(healthStatus);
        this.vaccinated.set(vaccinated);
        this.description.set(description);
    }
    
    // Getters and Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }
    
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }
    
    public String getType() { return type.get(); }
    public void setType(String type) { this.type.set(type); }
    public StringProperty typeProperty() { return type; }
    
    public String getBreed() { return breed.get(); }
    public void setBreed(String breed) { this.breed.set(breed); }
    public StringProperty breedProperty() { return breed; }
    
    public int getAge() { return age.get(); }
    public void setAge(int age) { this.age.set(age); }
    public IntegerProperty ageProperty() { return age; }
    
    public String getGender() { return gender.get(); }
    public void setGender(String gender) { this.gender.set(gender); }
    public StringProperty genderProperty() { return gender; }
    
    public String getHealthStatus() { return healthStatus.get(); }
    public void setHealthStatus(String healthStatus) { this.healthStatus.set(healthStatus); }
    public StringProperty healthStatusProperty() { return healthStatus; }
    
    public boolean isVaccinated() { return vaccinated.get(); }
    public void setVaccinated(boolean vaccinated) { this.vaccinated.set(vaccinated); }
    public BooleanProperty vaccinatedProperty() { return vaccinated; }
    
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }
    
    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }
    
    public LocalDate getAddedDate() { return addedDate.get(); }
    public void setAddedDate(LocalDate addedDate) { this.addedDate.set(addedDate); }
    public ObjectProperty<LocalDate> addedDateProperty() { return addedDate; }
    
    @Override
    public String toString() {
        return name.get() + " (" + type.get() + ")";
    }
}
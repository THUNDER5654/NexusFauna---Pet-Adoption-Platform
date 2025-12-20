package services;

import dao.AdoptionDAO;
import dao.PetDAO;
import models.Adoption;
import models.Pet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AdoptionService {
    private AdoptionDAO adoptionDAO;
    private PetDAO petDAO;
    
    public AdoptionService() {
        this.adoptionDAO = new AdoptionDAO();
        this.petDAO = new PetDAO();
    }
    
    public String requestAdoption(int petId, int adopterId, String notes) {
        try {
            // Check if pet is available
            Pet pet = petDAO.getPetById(petId);
            if (pet == null) {
                return "Pet not found";
            }
            
            if (!"Available".equals(pet.getStatus())) {
                return "Pet is not available for adoption";
            }
            
            // Create adoption request
            Adoption adoption = new Adoption();
            adoption.setPetId(petId);
            adoption.setAdopterId(adopterId);
            adoption.setAdoptionDate(LocalDate.now());
            adoption.setStatus("Pending");
            adoption.setNotes(notes.trim());
            
            // Save with transaction
            boolean success = adoptionDAO.requestAdoption(adoption);
            
            return success ? "Adoption request submitted successfully!" 
                          : "Failed to submit adoption request";
            
        } catch (SQLException e) {
            System.err.println("Error requesting adoption: " + e.getMessage());
            return "Database error: " + e.getMessage();
        }
    }
    
    public String processAdoption(int adoptionId, boolean approve) {
        try {
            // Get current admin ID from session (in real app)
            int adminId = 1; // Default admin ID
            
            boolean success = adoptionDAO.processAdoption(adoptionId, approve, adminId);
            
            if (success) {
                return approve ? "Adoption approved successfully!" 
                              : "Adoption rejected successfully!";
            } else {
                return "Failed to process adoption request";
            }
        } catch (SQLException e) {
            System.err.println("Error processing adoption: " + e.getMessage());
            return "Database error: " + e.getMessage();
        }
    }
    
    public List<Adoption> getPendingAdoptions() {
        try {
            return adoptionDAO.getPendingAdoptions();
        } catch (SQLException e) {
            System.err.println("Error getting pending adoptions: " + e.getMessage());
            return List.of();
        }
    }
    
    public List<Adoption> getAdopterAdoptions(int adopterId) {
        try {
            return adoptionDAO.getAdoptionsByAdopter(adopterId);
        } catch (SQLException e) {
            System.err.println("Error getting adopter adoptions: " + e.getMessage());
            return List.of();
        }
    }
    
    public List<Adoption> getAllAdoptions() {
        try {
            return adoptionDAO.getAllAdoptions();
        } catch (SQLException e) {
            System.err.println("Error getting all adoptions: " + e.getMessage());
            return List.of();
        }
    }
    
    public Adoption getAdoptionById(int adoptionId) {
        try {
            return adoptionDAO.getAdoptionById(adoptionId);
        } catch (SQLException e) {
            System.err.println("Error getting adoption by ID: " + e.getMessage());
            return null;
        }
    }
}
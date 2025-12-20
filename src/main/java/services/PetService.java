package services;

import dao.PetDAO;
import models.Pet;
import java.sql.SQLException;
import java.util.List;

public class PetService {
    private PetDAO petDAO;
    
    public PetService() {
        this.petDAO = new PetDAO();
    }
    
    public boolean addPet(Pet pet) {
        try {
            return petDAO.addPet(pet);
        } catch (SQLException e) {
            System.err.println("Error adding pet: " + e.getMessage());
            return false;
        }
    }
    
    public List<Pet> getAllPets() {
        try {
            return petDAO.getAllPets();
        } catch (SQLException e) {
            System.err.println("Error getting pets: " + e.getMessage());
            return List.of();
        }
    }
    
    public List<Pet> getAvailablePets() {
        try {
            return petDAO.getAvailablePets();
        } catch (SQLException e) {
            System.err.println("Error getting available pets: " + e.getMessage());
            return List.of();
        }
    }
    
    public Pet getPetById(int id) {
        try {
            return petDAO.getPetById(id);
        } catch (SQLException e) {
            System.err.println("Error getting pet by ID: " + e.getMessage());
            return null;
        }
    }
    
    public boolean updatePet(Pet pet) {
        try {
            return petDAO.updatePet(pet);
        } catch (SQLException e) {
            System.err.println("Error updating pet: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updatePetStatus(int petId, String status) {
        try {
            return petDAO.updatePetStatus(petId, status);
        } catch (SQLException e) {
            System.err.println("Error updating pet status: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deletePet(int petId) {
        try {
            return petDAO.deletePet(petId);
        } catch (SQLException e) {
            System.err.println("Error deleting pet: " + e.getMessage());
            return false;
        }
    }
    
    public int getTotalPetsCount() {
        try {
            return petDAO.getTotalPetsCount();
        } catch (SQLException e) {
            System.err.println("Error getting total pets count: " + e.getMessage());
            return 0;
        }
    }
    
    public int getAvailablePetsCount() {
        try {
            return petDAO.getAvailablePetsCount();
        } catch (SQLException e) {
            System.err.println("Error getting available pets count: " + e.getMessage());
            return 0;
        }
    }
    
    public int getAdoptedPetsCount() {
        try {
            return petDAO.getAdoptedPetsCount();
        } catch (SQLException e) {
            System.err.println("Error getting adopted pets count: " + e.getMessage());
            return 0;
        }
    }
    
    public int getPendingPetsCount() {
        try {
            return petDAO.getPendingPetsCount();
        } catch (SQLException e) {
            System.err.println("Error getting pending pets count: " + e.getMessage());
            return 0;
        }
    }
}
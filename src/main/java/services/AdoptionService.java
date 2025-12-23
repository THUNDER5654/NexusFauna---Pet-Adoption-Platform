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
            
            // Check if user already has a pending request for this pet
            if (hasPendingRequest(adopterId, petId)) {
                return "You already have a pending adoption request for this pet";
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
            
            if (success) {
                // Update pet status to Pending
                petDAO.updatePetStatus(petId, "Pending");
                return "Adoption request submitted successfully!";
            } else {
                return "Failed to submit adoption request";
            }
            
        } catch (SQLException e) {
            System.err.println("Error requesting adoption: " + e.getMessage());
            return "Database error: " + e.getMessage();
        }
    }
    
    public String processAdoption(int adoptionId, boolean approve) {
        try {
            // Get adoption details first
            Adoption adoption = adoptionDAO.getAdoptionById(adoptionId);
            if (adoption == null) {
                return "Adoption request not found";
            }
            
            // Get current admin ID from session (in real app)
            int adminId = 1; // Default admin ID
            
            boolean success = adoptionDAO.processAdoption(adoptionId, approve, adminId);
            
            if (success) {
                // Update pet status based on approval/rejection
                String newStatus = approve ? "Adopted" : "Available";
                petDAO.updatePetStatus(adoption.getPetId(), newStatus);
                
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
    
    public boolean cancelAdoption(int adoptionId) {
        try {
            // Get adoption details first
            Adoption adoption = adoptionDAO.getAdoptionById(adoptionId);
            if (adoption == null) {
                return false;
            }
            
            // Only allow cancellation of pending requests
            if (!"Pending".equals(adoption.getStatus())) {
                return false;
            }
            
            boolean success = adoptionDAO.updateAdoptionStatus(adoptionId, "Cancelled");
            
            if (success) {
                // Update pet status back to Available
                petDAO.updatePetStatus(adoption.getPetId(), "Available");
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            System.err.println("Error cancelling adoption: " + e.getMessage());
            return false;
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
    
    public boolean hasPendingRequest(int adopterId, int petId) {
        try {
            return adoptionDAO.hasPendingRequest(adopterId, petId);
        } catch (SQLException e) {
            System.err.println("Error checking pending request: " + e.getMessage());
            return false;
        }
    }
    
    public List<Adoption> getRecentAdoptions(int limit) {
        try {
            return adoptionDAO.getRecentAdoptions(limit);
        } catch (SQLException e) {
            System.err.println("Error getting recent adoptions: " + e.getMessage());
            return List.of();
        }
    }
    
    public int getAdoptionCountByStatus(String status) {
        try {
            return adoptionDAO.getAdoptionCountByStatus(status);
        } catch (SQLException e) {
            System.err.println("Error getting adoption count by status: " + e.getMessage());
            return 0;
        }
    }
    
    public boolean updateAdoptionNotes(int adoptionId, String notes) {
        try {
            return adoptionDAO.updateAdoptionNotes(adoptionId, notes);
        } catch (SQLException e) {
            System.err.println("Error updating adoption notes: " + e.getMessage());
            return false;
        }
    }
    
    // Get adoption statistics for dashboard
    public AdoptionStats getAdoptionStats() {
        try {
            AdoptionStats stats = new AdoptionStats();
            stats.setTotalAdoptions(adoptionDAO.getTotalAdoptions());
            stats.setPendingCount(adoptionDAO.getAdoptionCountByStatus("Pending"));
            stats.setApprovedCount(adoptionDAO.getAdoptionCountByStatus("Approved"));
            stats.setRejectedCount(adoptionDAO.getAdoptionCountByStatus("Rejected"));
            stats.setCancelledCount(adoptionDAO.getAdoptionCountByStatus("Cancelled"));
            stats.setCompletedCount(adoptionDAO.getAdoptionCountByStatus("Completed"));
            return stats;
        } catch (SQLException e) {
            System.err.println("Error getting adoption stats: " + e.getMessage());
            return new AdoptionStats();
        }
    }
    
    // Get adopter-specific statistics
    public AdopterStats getAdopterStats(int adopterId) {
        try {
            AdopterStats stats = new AdopterStats();
            List<Adoption> adoptions = adoptionDAO.getAdoptionsByAdopter(adopterId);
            
            stats.setTotalRequests(adoptions.size());
            stats.setPendingRequests((int) adoptions.stream()
                .filter(a -> "Pending".equals(a.getStatus()))
                .count());
            stats.setApprovedRequests((int) adoptions.stream()
                .filter(a -> "Approved".equals(a.getStatus()))
                .count());
            stats.setRejectedRequests((int) adoptions.stream()
                .filter(a -> "Rejected".equals(a.getStatus()))
                .count());
            stats.setCancelledRequests((int) adoptions.stream()
                .filter(a -> "Cancelled".equals(a.getStatus()))
                .count());
            
            return stats;
        } catch (SQLException e) {
            System.err.println("Error getting adopter stats: " + e.getMessage());
            return new AdopterStats();
        }
    }
    
    // Inner class for adoption statistics
    public static class AdoptionStats {
        private int totalAdoptions;
        private int pendingCount;
        private int approvedCount;
        private int rejectedCount;
        private int cancelledCount;
        private int completedCount;
        
        // Getters and setters
        public int getTotalAdoptions() { return totalAdoptions; }
        public void setTotalAdoptions(int totalAdoptions) { this.totalAdoptions = totalAdoptions; }
        
        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
        
        public int getApprovedCount() { return approvedCount; }
        public void setApprovedCount(int approvedCount) { this.approvedCount = approvedCount; }
        
        public int getRejectedCount() { return rejectedCount; }
        public void setRejectedCount(int rejectedCount) { this.rejectedCount = rejectedCount; }
        
        public int getCancelledCount() { return cancelledCount; }
        public void setCancelledCount(int cancelledCount) { this.cancelledCount = cancelledCount; }
        
        public int getCompletedCount() { return completedCount; }
        public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
    }
    
    // Inner class for adopter statistics
    public static class AdopterStats {
        private int totalRequests;
        private int pendingRequests;
        private int approvedRequests;
        private int rejectedRequests;
        private int cancelledRequests;
        
        // Getters and setters
        public int getTotalRequests() { return totalRequests; }
        public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }
        
        public int getPendingRequests() { return pendingRequests; }
        public void setPendingRequests(int pendingRequests) { this.pendingRequests = pendingRequests; }
        
        public int getApprovedRequests() { return approvedRequests; }
        public void setApprovedRequests(int approvedRequests) { this.approvedRequests = approvedRequests; }
        
        public int getRejectedRequests() { return rejectedRequests; }
        public void setRejectedRequests(int rejectedRequests) { this.rejectedRequests = rejectedRequests; }
        
        public int getCancelledRequests() { return cancelledRequests; }
        public void setCancelledRequests(int cancelledRequests) { this.cancelledRequests = cancelledRequests; }
    }
}
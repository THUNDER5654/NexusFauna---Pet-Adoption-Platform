package dao;

import models.Adoption;
import models.Pet;
import models.User;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdoptionDAO {
    
    // Existing methods
    public boolean requestAdoption(Adoption adoption) throws SQLException {
        String sql = "INSERT INTO adoptions (pet_id, adopter_id, adoption_date, status, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, adoption.getPetId());
            stmt.setInt(2, adoption.getAdopterId());
            stmt.setDate(3, Date.valueOf(adoption.getAdoptionDate()));
            stmt.setString(4, adoption.getStatus());
            stmt.setString(5, adoption.getNotes());
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean processAdoption(int adoptionId, boolean approve, int adminId) throws SQLException {
        String status = approve ? "Approved" : "Rejected";
        String sql = "UPDATE adoptions SET status = ?, processed_by = ?, processed_date = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, adminId);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setInt(4, adoptionId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public List<Adoption> getPendingAdoptions() throws SQLException {
        String sql = "SELECT a.*, p.*, u.* FROM adoptions a " +
                     "JOIN pets p ON a.pet_id = p.id " +
                     "JOIN users u ON a.adopter_id = u.id " +
                     "WHERE a.status = 'Pending' " +
                     "ORDER BY a.adoption_date DESC";
        return getAdoptionsWithDetails(sql);
    }
    
    public List<Adoption> getAdoptionsByAdopter(int adopterId) throws SQLException {
        String sql = "SELECT a.*, p.*, u.* FROM adoptions a " +
                     "JOIN pets p ON a.pet_id = p.id " +
                     "JOIN users u ON a.adopter_id = u.id " +
                     "WHERE a.adopter_id = ? " +
                     "ORDER BY a.adoption_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, adopterId);
            ResultSet rs = stmt.executeQuery();
            return mapResultSetToAdoptions(rs);
        }
    }
    
    public List<Adoption> getAllAdoptions() throws SQLException {
        String sql = "SELECT a.*, p.*, u.* FROM adoptions a " +
                     "JOIN pets p ON a.pet_id = p.id " +
                     "JOIN users u ON a.adopter_id = u.id " +
                     "ORDER BY a.adoption_date DESC";
        return getAdoptionsWithDetails(sql);
    }
    
    public Adoption getAdoptionById(int adoptionId) throws SQLException {
        String sql = "SELECT a.*, p.*, u.* FROM adoptions a " +
                     "JOIN pets p ON a.pet_id = p.id " +
                     "JOIN users u ON a.adopter_id = u.id " +
                     "WHERE a.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, adoptionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToAdoption(rs);
            }
        }
        return null;
    }
    
    // NEW METHODS ADDED:
    
    public boolean hasPendingRequest(int adopterId, int petId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM adoptions WHERE adopter_id = ? AND pet_id = ? AND status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, adopterId);
            stmt.setInt(2, petId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    public boolean updateAdoptionStatus(int adoptionId, String status) throws SQLException {
        String sql = "UPDATE adoptions SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, adoptionId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean updateAdoptionNotes(int adoptionId, String notes) throws SQLException {
        String sql = "UPDATE adoptions SET notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, notes);
            stmt.setInt(2, adoptionId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public List<Adoption> getRecentAdoptions(int limit) throws SQLException {
        String sql = "SELECT a.*, p.*, u.* FROM adoptions a " +
                     "JOIN pets p ON a.pet_id = p.id " +
                     "JOIN users u ON a.adopter_id = u.id " +
                     "ORDER BY a.adoption_date DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            return mapResultSetToAdoptions(rs);
        }
    }
    
    public int getAdoptionCountByStatus(String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM adoptions WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    public int getTotalAdoptions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM adoptions";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    // HELPER METHODS:
    
    private List<Adoption> getAdoptionsWithDetails(String sql) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return mapResultSetToAdoptions(rs);
        }
    }
    
    private List<Adoption> mapResultSetToAdoptions(ResultSet rs) throws SQLException {
        List<Adoption> adoptions = new ArrayList<>();
        while (rs.next()) {
            adoptions.add(mapResultSetToAdoption(rs));
        }
        return adoptions;
    }
    
    private Adoption mapResultSetToAdoption(ResultSet rs) throws SQLException {
        Adoption adoption = new Adoption();
        adoption.setId(rs.getInt("a.id"));
        adoption.setPetId(rs.getInt("pet_id"));
        adoption.setAdopterId(rs.getInt("adopter_id"));
        adoption.setAdoptionDate(rs.getDate("adoption_date").toLocalDate());
        adoption.setStatus(rs.getString("status"));
        adoption.setNotes(rs.getString("notes"));
        
        // Create and set Pet object
        Pet pet = new Pet();
        pet.setId(rs.getInt("p.id"));
        pet.setName(rs.getString("name"));
        pet.setType(rs.getString("type"));
        pet.setBreed(rs.getString("breed"));
        pet.setAge(rs.getInt("age"));
        pet.setGender(rs.getString("gender"));
        pet.setHealthStatus(rs.getString("health_status"));
        pet.setVaccinated(rs.getBoolean("vaccinated"));
        pet.setDescription(rs.getString("description"));
        pet.setStatus(rs.getString("status"));
        adoption.setPet(pet);
        
        // Create and set User object
        User user = new User();
        user.setId(rs.getInt("u.id"));
        user.setUsername(rs.getString("username"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        adoption.setAdopter(user);
        
        return adoption;
    }
}
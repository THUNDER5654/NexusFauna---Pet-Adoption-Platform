package dao;

import models.Adoption;
import models.Pet;
import models.User;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdoptionDAO {
    
    // Request adoption with TRANSACTION
    public boolean requestAdoption(Adoption adoption) throws SQLException {
        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // 1. Check if pet is available (with lock for consistency)
            String checkSql = "SELECT adoption_status FROM pets WHERE id = ? FOR UPDATE";
            checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, adoption.getPetId());
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next() || !"Available".equals(rs.getString("adoption_status"))) {
                conn.rollback();
                return false; // Pet not available
            }
            
            // 2. Insert adoption record
            String insertSql = "INSERT INTO adoptions (pet_id, adopter_id, adoption_date, status, notes) " +
                             "VALUES (?, ?, ?, ?, ?)";
            insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setInt(1, adoption.getPetId());
            insertStmt.setInt(2, adoption.getAdopterId());
            insertStmt.setDate(3, Date.valueOf(adoption.getAdoptionDate()));
            insertStmt.setString(4, adoption.getStatus());
            insertStmt.setString(5, adoption.getNotes());
            
            int rowsInserted = insertStmt.executeUpdate();
            
            if (rowsInserted == 0) {
                conn.rollback();
                return false;
            }
            
            // Get generated adoption ID
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    adoption.setId(generatedKeys.getInt(1));
                }
            }
            
            // 3. Update pet status to Pending
            String updateSql = "UPDATE pets SET adoption_status = 'Pending' WHERE id = ?";
            updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, adoption.getPetId());
            int rowsUpdated = updateStmt.executeUpdate();
            
            if (rowsUpdated == 0) {
                conn.rollback();
                return false;
            }
            
            conn.commit(); // Commit transaction
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback on error
            }
            throw e;
        } finally {
            // Restore auto-commit and close resources
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (checkStmt != null) checkStmt.close();
            if (insertStmt != null) insertStmt.close();
            if (updateStmt != null) updateStmt.close();
        }
    }
    
    // Process adoption (approve/reject) with TRANSACTION
    public boolean processAdoption(int adoptionId, boolean approve, int adminId) throws SQLException {
        Connection conn = null;
        PreparedStatement adoptionStmt = null;
        PreparedStatement petStmt = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            String adoptionSql;
            String petSql;
            
            if (approve) {
                adoptionSql = "UPDATE adoptions SET status = 'Approved', processed_by = ?, processed_date = ? WHERE id = ?";
                petSql = "UPDATE pets SET adoption_status = 'Adopted' WHERE id = (SELECT pet_id FROM adoptions WHERE id = ?)";
            } else {
                adoptionSql = "UPDATE adoptions SET status = 'Rejected', processed_by = ?, processed_date = ? WHERE id = ?";
                petSql = "UPDATE pets SET adoption_status = 'Available' WHERE id = (SELECT pet_id FROM adoptions WHERE id = ?)";
            }
            
            // Update adoption record
            adoptionStmt = conn.prepareStatement(adoptionSql);
            adoptionStmt.setInt(1, adminId);
            adoptionStmt.setDate(2, Date.valueOf(LocalDate.now()));
            adoptionStmt.setInt(3, adoptionId);
            
            int adoptionUpdated = adoptionStmt.executeUpdate();
            
            if (adoptionUpdated == 0) {
                conn.rollback();
                return false;
            }
            
            // Update pet status
            petStmt = conn.prepareStatement(petSql);
            petStmt.setInt(1, adoptionId);
            
            int petUpdated = petStmt.executeUpdate();
            
            if (petUpdated == 0) {
                conn.rollback();
                return false;
            }
            
            conn.commit(); // Commit transaction
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Rollback on error
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (adoptionStmt != null) adoptionStmt.close();
            if (petStmt != null) petStmt.close();
        }
    }
    
    public List<Adoption> getPendingAdoptions() throws SQLException {
        List<Adoption> adoptions = new ArrayList<>();
        String sql = """
            SELECT 
                a.*,
                p.name as pet_name,
                p.type as pet_type,
                p.breed as pet_breed,
                p.age as pet_age,
                p.gender as pet_gender,
                u.full_name as adopter_name,
                u.email as adopter_email,
                u.phone as adopter_phone
            FROM adoptions a
            JOIN pets p ON a.pet_id = p.id
            JOIN users u ON a.adopter_id = u.id
            WHERE a.status = 'Pending'
            ORDER BY a.adoption_date DESC
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Adoption adoption = extractAdoptionFromResultSet(rs);
                
                // Set pet details
                Pet pet = new Pet();
                pet.setId(rs.getInt("pet_id"));
                pet.setName(rs.getString("pet_name"));
                pet.setType(rs.getString("pet_type"));
                pet.setBreed(rs.getString("pet_breed"));
                pet.setAge(rs.getInt("pet_age"));
                pet.setGender(rs.getString("pet_gender"));
                adoption.setPet(pet);
                
                // Set adopter details
                User adopter = new User();
                adopter.setId(rs.getInt("adopter_id"));
                adopter.setFullName(rs.getString("adopter_name"));
                adopter.setEmail(rs.getString("adopter_email"));
                adopter.setPhone(rs.getString("adopter_phone"));
                adoption.setAdopter(adopter);
                
                adoptions.add(adoption);
            }
        }
        return adoptions;
    }
    
    public List<Adoption> getAdoptionsByAdopter(int adopterId) throws SQLException {
        List<Adoption> adoptions = new ArrayList<>();
        String sql = """
            SELECT 
                a.*,
                p.name as pet_name,
                p.type as pet_type,
                p.breed as pet_breed,
                p.adoption_status as pet_status
            FROM adoptions a
            JOIN pets p ON a.pet_id = p.id
            WHERE a.adopter_id = ?
            ORDER BY a.adoption_date DESC
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, adopterId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Adoption adoption = extractAdoptionFromResultSet(rs);
                    
                    Pet pet = new Pet();
                    pet.setName(rs.getString("pet_name"));
                    pet.setType(rs.getString("pet_type"));
                    pet.setBreed(rs.getString("pet_breed"));
                    pet.setStatus(rs.getString("pet_status"));
                    adoption.setPet(pet);
                    
                    adoptions.add(adoption);
                }
            }
        }
        return adoptions;
    }
    
    public List<Adoption> getAllAdoptions() throws SQLException {
        List<Adoption> adoptions = new ArrayList<>();
        String sql = """
            SELECT 
                a.*,
                p.name as pet_name,
                u.full_name as adopter_name
            FROM adoptions a
            JOIN pets p ON a.pet_id = p.id
            JOIN users u ON a.adopter_id = u.id
            ORDER BY a.adoption_date DESC
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Adoption adoption = extractAdoptionFromResultSet(rs);
                
                Pet pet = new Pet();
                pet.setName(rs.getString("pet_name"));
                adoption.setPet(pet);
                
                User adopter = new User();
                adopter.setFullName(rs.getString("adopter_name"));
                adoption.setAdopter(adopter);
                
                adoptions.add(adoption);
            }
        }
        return adoptions;
    }
    
    public Adoption getAdoptionById(int adoptionId) throws SQLException {
        String sql = """
            SELECT 
                a.*,
                p.name as pet_name,
                p.type as pet_type,
                p.breed as pet_breed,
                u.full_name as adopter_name,
                u.email as adopter_email,
                u.phone as adopter_phone
            FROM adoptions a
            JOIN pets p ON a.pet_id = p.id
            JOIN users u ON a.adopter_id = u.id
            WHERE a.id = ?
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, adoptionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Adoption adoption = extractAdoptionFromResultSet(rs);
                    
                    Pet pet = new Pet();
                    pet.setName(rs.getString("pet_name"));
                    pet.setType(rs.getString("pet_type"));
                    pet.setBreed(rs.getString("pet_breed"));
                    adoption.setPet(pet);
                    
                    User adopter = new User();
                    adopter.setFullName(rs.getString("adopter_name"));
                    adopter.setEmail(rs.getString("adopter_email"));
                    adopter.setPhone(rs.getString("adopter_phone"));
                    adoption.setAdopter(adopter);
                    
                    return adoption;
                }
            }
        }
        return null;
    }
    
    private Adoption extractAdoptionFromResultSet(ResultSet rs) throws SQLException {
        Adoption adoption = new Adoption();
        adoption.setId(rs.getInt("id"));
        adoption.setPetId(rs.getInt("pet_id"));
        adoption.setAdopterId(rs.getInt("adopter_id"));
        adoption.setAdoptionDate(rs.getDate("adoption_date").toLocalDate());
        adoption.setStatus(rs.getString("status"));
        adoption.setNotes(rs.getString("notes"));
        return adoption;
    }
}
package dao;

import models.Pet;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PetDAO {
    
    // Existing methods
    public List<Pet> getAllPets() throws SQLException {
        String sql = "SELECT * FROM pets ORDER BY name";
        List<Pet> pets = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                pets.add(mapResultSetToPet(rs));
            }
        }
        return pets;
    }
    
    public List<Pet> getAvailablePets() throws SQLException {
        String sql = "SELECT * FROM pets WHERE status = 'Available' ORDER BY name";
        List<Pet> pets = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                pets.add(mapResultSetToPet(rs));
            }
        }
        return pets;
    }
    
    public Pet getPetById(int id) throws SQLException {
        String sql = "SELECT * FROM pets WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPet(rs);
            }
        }
        return null;
    }
    
    public boolean addPet(Pet pet) throws SQLException {
        String sql = "INSERT INTO pets (name, type, breed, age, gender, health_status, vaccinated, description, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pet.getName());
            stmt.setString(2, pet.getType());
            stmt.setString(3, pet.getBreed());
            stmt.setInt(4, pet.getAge());
            stmt.setString(5, pet.getGender());
            stmt.setString(6, pet.getHealthStatus());
            stmt.setBoolean(7, pet.isVaccinated());
            stmt.setString(8, pet.getDescription());
            stmt.setString(9, pet.getStatus() != null ? pet.getStatus() : "Available");
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean updatePet(Pet pet) throws SQLException {
        String sql = "UPDATE pets SET name = ?, type = ?, breed = ?, age = ?, gender = ?, " +
                     "health_status = ?, vaccinated = ?, description = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pet.getName());
            stmt.setString(2, pet.getType());
            stmt.setString(3, pet.getBreed());
            stmt.setInt(4, pet.getAge());
            stmt.setString(5, pet.getGender());
            stmt.setString(6, pet.getHealthStatus());
            stmt.setBoolean(7, pet.isVaccinated());
            stmt.setString(8, pet.getDescription());
            stmt.setString(9, pet.getStatus());
            stmt.setInt(10, pet.getId());
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean deletePet(int id) throws SQLException {
        // First check if pet has any pending adoptions
        String checkSql = "SELECT COUNT(*) FROM adoptions WHERE pet_id = ? AND status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false; // Cannot delete pet with pending adoptions
            }
        }
        
        String sql = "DELETE FROM pets WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }
    
    public int getTotalPetsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pets";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    public int getAvailablePetsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pets WHERE status = 'Available'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    public int getAdoptedPetsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pets WHERE status = 'Adopted'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    public int getPendingPetsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pets WHERE status = 'Pending'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    
    // NEW METHOD ADDED:
    public boolean updatePetStatus(int petId, String status) throws SQLException {
        String sql = "UPDATE pets SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, petId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Search and filter methods
    public List<Pet> searchPets(String searchTerm, String type, String gender, int maxAge) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM pets WHERE status = 'Available'");
        List<Object> params = new ArrayList<>();
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(breed) LIKE ?)");
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        if (type != null && !type.equals("All Types") && !type.isEmpty()) {
            sql.append(" AND type = ?");
            params.add(type);
        }
        
        if (gender != null && !gender.equals("All Genders") && !gender.isEmpty()) {
            sql.append(" AND gender = ?");
            params.add(gender);
        }
        
        if (maxAge > 0) {
            sql.append(" AND age <= ?");
            params.add(maxAge);
        }
        
        sql.append(" ORDER BY name");
        
        List<Pet> pets = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pets.add(mapResultSetToPet(rs));
            }
        }
        return pets;
    }
    
    // HELPER METHOD:
    private Pet mapResultSetToPet(ResultSet rs) throws SQLException {
        Pet pet = new Pet();
        pet.setId(rs.getInt("id"));
        pet.setName(rs.getString("name"));
        pet.setType(rs.getString("type"));
        pet.setBreed(rs.getString("breed"));
        pet.setAge(rs.getInt("age"));
        pet.setGender(rs.getString("gender"));
        pet.setHealthStatus(rs.getString("health_status"));
        pet.setVaccinated(rs.getBoolean("vaccinated"));
        pet.setDescription(rs.getString("description"));
        pet.setStatus(rs.getString("status"));
        return pet;
    }
}
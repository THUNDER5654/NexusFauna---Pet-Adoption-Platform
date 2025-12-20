package dao;

import models.Pet;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PetDAO {
    
    public boolean addPet(Pet pet) throws SQLException {
        String sql = "INSERT INTO pets (name, type, breed, age, gender, health_status, " +
                    "vaccination_status, description, adoption_status, added_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, pet.getName());
            pstmt.setString(2, pet.getType());
            pstmt.setString(3, pet.getBreed());
            pstmt.setInt(4, pet.getAge());
            pstmt.setString(5, pet.getGender());
            pstmt.setString(6, pet.getHealthStatus());
            pstmt.setBoolean(7, pet.isVaccinated());
            pstmt.setString(8, pet.getDescription());
            pstmt.setString(9, pet.getStatus());
            pstmt.setDate(10, Date.valueOf(pet.getAddedDate()));
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        pet.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }
    
    public List<Pet> getAllPets() throws SQLException {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT * FROM pets ORDER BY added_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Pet pet = extractPetFromResultSet(rs);
                pets.add(pet);
            }
        }
        return pets;
    }
    
    public List<Pet> getAvailablePets() throws SQLException {
        List<Pet> pets = new ArrayList<>();
        String sql = "SELECT * FROM pets WHERE adoption_status = 'Available' ORDER BY added_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Pet pet = extractPetFromResultSet(rs);
                pets.add(pet);
            }
        }
        return pets;
    }
    
    public Pet getPetById(int id) throws SQLException {
        String sql = "SELECT * FROM pets WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractPetFromResultSet(rs);
                }
            }
        }
        return null;
    }
    
    public boolean updatePet(Pet pet) throws SQLException {
        String sql = "UPDATE pets SET name = ?, type = ?, breed = ?, age = ?, " +
                    "gender = ?, health_status = ?, vaccination_status = ?, " +
                    "description = ?, adoption_status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, pet.getName());
            pstmt.setString(2, pet.getType());
            pstmt.setString(3, pet.getBreed());
            pstmt.setInt(4, pet.getAge());
            pstmt.setString(5, pet.getGender());
            pstmt.setString(6, pet.getHealthStatus());
            pstmt.setBoolean(7, pet.isVaccinated());
            pstmt.setString(8, pet.getDescription());
            pstmt.setString(9, pet.getStatus());
            pstmt.setInt(10, pet.getId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean updatePetStatus(int petId, String status) throws SQLException {
        String sql = "UPDATE pets SET adoption_status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, petId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean deletePet(int petId) throws SQLException {
        String sql = "DELETE FROM pets WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, petId);
            return pstmt.executeUpdate() > 0;
        }
    }
    
    public int getTotalPetsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pets";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int getAvailablePetsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pets WHERE adoption_status = 'Available'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int getAdoptedPetsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pets WHERE adoption_status = 'Adopted'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int getPendingPetsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM pets WHERE adoption_status = 'Pending'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    private Pet extractPetFromResultSet(ResultSet rs) throws SQLException {
        Pet pet = new Pet();
        pet.setId(rs.getInt("id"));
        pet.setName(rs.getString("name"));
        pet.setType(rs.getString("type"));
        pet.setBreed(rs.getString("breed"));
        pet.setAge(rs.getInt("age"));
        pet.setGender(rs.getString("gender"));
        pet.setHealthStatus(rs.getString("health_status"));
        pet.setVaccinated(rs.getBoolean("vaccination_status"));
        pet.setDescription(rs.getString("description"));
        pet.setStatus(rs.getString("adoption_status"));
        pet.setAddedDate(rs.getDate("added_date").toLocalDate());
        return pet;
    }
}
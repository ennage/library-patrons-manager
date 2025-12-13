package library.utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import handlers.DBConnector;
import library.models.Category;

public class CategoryDAO {
    // -------------------------------------------------
    // --- ID GENERATION LOGIC (Special Requirement) ---
    // -------------------------------------------------
    private String generateNextId(String categoryName, Connection conn) throws SQLException {
        // 1. Create 4-letter prefix (e.g., FICT for Fiction)
        String prefix = categoryName.toUpperCase().substring(0, Math.min(categoryName.length(), 4));
        // 2. Query to find the highest sequence number for this prefix
        String sql = "SELECT CategoryID FROM categories WHERE CategoryID LIKE ? ORDER BY CategoryID DESC LIMIT 1";
        
        int nextSequence = 1;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Use the prefix + wildcard (%) to search, e.g., "FICT%"
            pstmt.setString(1, prefix + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String lastId = rs.getString("CategoryID");
                    // Extract the number part (3 digits)
                    String numberPart = lastId.substring(prefix.length());
                    // Convert to integer and increment
                    nextSequence = Integer.parseInt(numberPart) + 1;
                }
            }
        }
        
        // 3. Format the new sequence number with zero-padding (001, 010, 100)
        DecimalFormat df = new DecimalFormat("000"); // 3 sequential digits
        String newSequence = df.format(nextSequence); 

        // 4. Return the new full ID
        return prefix + newSequence; // e.g., "FICT006"
    }

    // -------------------------------------
    // --- CRUD OPERATION IMPLEMENTATION ---
    // -------------------------------------

    // ---------- CREATE ----------
    public void createCategory(String categoryName) throws SQLException {
        Connection link = null;
        PreparedStatement state = null;
        try {
            link = DBConnector.getConnection();
            
            // 1. Generate the unique, sequential ID before inserting
            String newId = generateNextId(categoryName, link);
            
            String sql = "INSERT INTO categories (CategoryID, CategoryName) VALUES (?, ?)";
            state = link.prepareStatement(sql);
            
            state.setString(1, newId);
            state.setString(2, categoryName);
            state.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error creating category: " + e.getMessage());
            throw e; 
        } finally {
            if (state != null) state.close();
            if (link != null) link.close();
        }
    }

    // ---------- READ ----------
    public List<Category> readAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY CategoryName";
        
        Connection link = null;
        PreparedStatement state = null;
        ResultSet resultSet = null;
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            resultSet = state.executeQuery();
            
            while (resultSet.next()) {
                String categoryId = resultSet.getString("CategoryID");
                String categoryName = resultSet.getString("CategoryName");
                
                Category category = new Category(categoryId, categoryName);
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("Error reading all categories: " + e.getMessage());
            throw e; 
        } finally {
            if (resultSet != null) resultSet.close();
            if (state != null) state.close();
            if (link != null) link.close();
        }
        return categories;
    }

    // ---------- UPDATE ----------
    public void updateCategory(Category category) throws SQLException {
        String sql = "UPDATE categories SET CategoryName = ? WHERE CategoryID = ?";
        
        Connection link = null;
        PreparedStatement state = null;
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            
            state.setString(1, category.getCategoryName());
            state.setString(2, category.getCategoryID());
            state.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating category: " + e.getMessage());
            throw e;
        } finally {
            if (state != null) state.close();
            if (link != null) link.close();
        }
    }

    // ---------- DELETE ----------
    public void deleteCategory(String categoryID) throws SQLException {
        String sql = "DELETE FROM categories WHERE CategoryID = ?";

        Connection link = null;
        PreparedStatement state = null;
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            
            state.setString(1, categoryID);
            int rowsAffected = state.executeUpdate();
            
            if (rowsAffected == 0) {
                System.out.println("No category found with ID: " + categoryID);
            }
        } catch (SQLException e) {
            // Handle Foreign Key Constraint error (if the category is referenced by a book)
            if (e.getSQLState().startsWith("23")) { 
                System.err.println("Cannot delete category. Books are currently assigned to this category.");
            }
            throw e;
        } finally {
            if (state != null) state.close();
            if (link != null) link.close();
        }
    }
}
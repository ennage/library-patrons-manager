package library.utilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import configuration.DBConnector;
import library.models.Category;

public class CategoryDAO {
    // -------------------------------------
    // --- CRUD OPERATION IMPLEMENTATION ---
    // -------------------------------------

    // ---------- CREATE ----------
    public Category createCategory(Category category) throws SQLException { // Changed signature to take Category object
        Connection link = null;
        PreparedStatement state = null;
        
        // SQL statement remains the same, but values come from the Category object
        String sql = "INSERT INTO categories (CategoryID, CategoryName) VALUES (?, ?)";
        
        try {
            link = DBConnector.getConnection();
            state = link.prepareStatement(sql);
            
            // 1. Use the ID provided by the Category object (user input)
            state.setString(1, category.getCategoryID()); 
            state.setString(2, category.getCategoryName());
            state.executeUpdate();
            
            // Return the object that was saved
            return category;
            
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
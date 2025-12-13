package handlers;

import java.sql.SQLException;
import java.util.List;

import library.models.Category;
import library.models.Patron;
import library.utilities.CategoryDAO;
import library.utilities.PatronDAO;

public class TesterForDAO {
    // --- Helper method to display a title for a test block ---
    private static void printHeader(String title) {
        System.out.println("\n--------------------------------------------------");
        System.out.println("  " + title);
        System.out.println("--------------------------------------------------");
    }

    public static void main(String[] args) {
        PatronDAO patronDAO = new PatronDAO();
        CategoryDAO categoryDAO = new CategoryDAO();
        
        try {
            // ==========================================================
            // TEST 1: READ ALL PATRONS (Verify Sample Data)
            // ==========================================================
            printHeader("TEST 1: Reading All Patrons (Existing Sample Data)");
            
            List<Patron> patrons = patronDAO.readAllPatrons();
            System.out.println("Retrieved " + patrons.size() + " patrons.");
            
            for (Patron p : patrons) {
                System.out.printf("  ID: %s, Name: %s %s, Email: %s\n", 
                    p.getPatronID(), p.getFirstName(), p.getLastName(), p.getEmail());
            }

            // ==========================================================
            // TEST 2: CREATE PATRON (Create new record)
            // ==========================================================
            printHeader("TEST 2: Creating a New Patron");
            
            // NOTE: We manually generate a sequential ID here for the Patron table.
            // In a real app, you would have a separate method for this, similar to CategoryDAO.
            String newPatronId = "P0007"; 
            Patron newPatron = new Patron(
                newPatronId,
                "Elias", 
                "Santos", 
                "Pasig City", 
                "elias.santos4@example.com", 
                "9051234570"
            );
            
            patronDAO.createPatron(newPatron);
            System.out.println("Patron created successfully with ID: " + newPatronId);

            // ==========================================================
            // TEST 3: READ ALL CATEGORIES (Verify Sample Data)
            // ==========================================================
            printHeader("TEST 3: Reading All Categories");
            
            List<Category> categories = categoryDAO.readAllCategories();
            System.out.println("Retrieved " + categories.size() + " categories.");
            
            for (Category c : categories) {
                System.out.printf("  ID: %s, Name: %s\n", c.getCategoryID(), c.getCategoryName());
            }

            // ==========================================================
            // TEST 4: Creating a New Category (Test ID Generator Logic)
            // ==========================================================
            printHeader("TEST 4: Creating a New Category (Testing ID Generator)");

            // --- FIX: Use a unique name to avoid the Duplicate entry error ---
            String newCategoryName = "Reference Books2"; // This should be unique

            // The DAO handles the ID generation logic internally
            categoryDAO.createCategory(newCategoryName); 
            System.out.println("Category '" + newCategoryName + "' created. Check the DB for the generated ID (e.g., 'REFE001').");


        } catch (SQLException e) {
            System.err.println("\n*** FATAL DATABASE ERROR DURING TESTING ***");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}